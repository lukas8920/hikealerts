package org.devbros.microsoft_hackathon.map_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MapLayerService {
    private static final String tmpFilePath = "layer_tmp.geojson";
    private static final String dstFilePath = "layer.geojson";
    private static final Logger logger = LoggerFactory.getLogger(MapLayerService.class.getName());

    private static final Object FILE_LOCK = new Object();
    private static final Object UPDATE_LOCK = new Object();

    private final WKBReader wkbReader;
    private final ITrailRepository iTrailRepository;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean hasWaitingThread = new AtomicBoolean(false);

    @Autowired
    public MapLayerService(ITrailRepository iTrailRepository){
        this.objectMapper = new ObjectMapper();
        this.wkbReader = new WKBReader();
        this.iTrailRepository = iTrailRepository;
    }

    public Resource loadJsonLayer() {
        synchronized (FILE_LOCK){
            return new FileSystemResource(dstFilePath);
        }
    }

    public void requestGeoJsonFileUpdate(){
        new Thread(() -> {
            //ignore request, if there is currently a thread waiting
            if (!hasWaitingThread.get()){
                logger.info("Disable geojson file update waiting queue.");
                //inform future requests that there is currently a thread waiting
                hasWaitingThread.set(true);

                synchronized (UPDATE_LOCK){
                    logger.info("Locked geojson file updating / enable wating queue.");
                    //inform future requests that there is currently no thread waiting
                    hasWaitingThread.set(false);
                    this.updateGeoJsonFile();
                }
            }
        }).start();
    }

    protected void updateGeoJsonFile() {
        this.fetchAndWriteGeoJsonToFile();

        try {
            logger.info("copy tmp file path to dst file path");
            synchronized (FILE_LOCK){
                Path source = Paths.get(tmpFilePath);
                Path destination = Paths.get(dstFilePath);
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e){
            logger.info("Could not replace existing geojson file.");
        } finally {
            logger.info("Delete tmp file.");
            this.deleteFile(tmpFilePath);
        }

        logger.info("Finished processing.");
    }

    private void fetchAndWriteGeoJsonToFile() {
        logger.info("fetch and write geojson to file");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFilePath))) {
            // Write the start of the FeatureCollection
            writer.write("{\"type\": \"FeatureCollection\", \"features\": [");

            // Example: Stream LineString data with titles
            int offset = 0;
            int limit = 1000;
            logger.info("query trails from the database.");
            List<Trail> trails = this.iTrailRepository.fetchTrails(offset, limit);
            logger.info("queried {} trails", trails.size());
            while (!trails.isEmpty()){
                boolean firstFeature = true;
                for (int i = 0; i < trails.size(); i++) {
                    // Stream feature, adding a comma only between features
                    if (!firstFeature) {
                        writer.write(",");
                    }
                    Trail trail = trails.get(i);
                    writer.write(convertLineStringToFeature(trail.getCoordinates(), trail.getTrailname()));

                    firstFeature = false;
                }
                offset += limit;

                trails = this.iTrailRepository.fetchTrails(offset, limit);
            }

            // Write the end of the FeatureCollection
            writer.write("]}");
        } catch (Exception e) {
            logger.error("Cancelled writing geojson file", e);
            this.deleteFile(tmpFilePath);
        }
    }

    private void deleteFile(String path){
        File file = new File(path);
        file.delete();
    }

    // Convert a single LineString and its title to a GeoJSON feature string
    private String convertLineStringToFeature(byte[] rawLine, String trailName) throws IOException, ParseException {
        LineString line = (LineString) this.wkbReader.read(rawLine);

        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        // Create geometry part of GeoJSON
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "LineString");

        // Convert LineString coordinates to GeoJSON format
        double[][] coordinates = new double[line.getCoordinates().length][2];
        for (int j = 0; j < line.getCoordinates().length; j++) {
            coordinates[j][0] = line.getCoordinateN(j).x;
            coordinates[j][1] = line.getCoordinateN(j).y;
        }
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);

        // Add properties (e.g., title)
        Map<String, Object> properties = new HashMap<>();
        properties.put("trail_name", trailName);
        feature.put("properties", properties);

        // Convert feature map to JSON string using Jackson
        return objectMapper.writeValueAsString(feature);
    }
}

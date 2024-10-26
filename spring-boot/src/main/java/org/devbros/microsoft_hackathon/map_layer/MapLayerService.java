package org.devbros.microsoft_hackathon.map_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.devbros.microsoft_hackathon.util.BaseScheduler;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MapLayerService extends BaseScheduler {
    private static final String tmpFilePath = "src/main/resources/static/layer_tmp.geojson";
    private static final String dstFilePath = "src/main/resources/static/layer.geojson";
    private static final Logger logger = LoggerFactory.getLogger(MapLayerService.class.getName());
    private static final Object LOCK = new Object();

    private final WKBReader wkbReader;
    private final ITrailRepository iTrailRepository;
    private final ObjectMapper objectMapper;

    private boolean finishedProcessing = false;
    private Thread currentThread;

    @Autowired
    public MapLayerService(ITrailRepository iTrailRepository){
        this.objectMapper = new ObjectMapper();
        this.wkbReader = new WKBReader();
        this.iTrailRepository = iTrailRepository;
    }

    public Resource loadJsonLayer(){
        synchronized (LOCK){
            return new ClassPathResource("static/layer.geojson");
        }
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void runProcedure() {
        while (running && !Thread.currentThread().isInterrupted()){
            this.finishedProcessing = false;
            this.fetchAndWriteGeoJsonToFile();

            try {
                synchronized (LOCK){
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

            logger.info("Finished processing");
            this.currentThread = Thread.currentThread();
            this.finishedProcessing = true;

            try {
                getLogger().info("Go to sleep until tomorrow.");
                TimeUnit.HOURS.sleep(24);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    protected void stopScheduledService(){
        logger.info("Stop service.");
        while (true){
            if (this.finishedProcessing){
                this.currentThread.interrupt();
                break;
            }
        }
    }

    private void fetchAndWriteGeoJsonToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFilePath))) {
            // Write the start of the FeatureCollection
            writer.write("{\"type\": \"FeatureCollection\", \"features\": [");

            // Example: Stream LineString data with titles
            int offset = 0;
            int limit = 1000;
            List<Trail> trails = this.iTrailRepository.fetchTrails(offset, limit);
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
        } catch (IOException | ParseException e) {
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

package org.hikingdev.microsoft_hackathon.map_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.zip.GZIPOutputStream;

@Service
public class MapLayerService {
    private static final String tmpFilePath = "layer_tmp.geojson.gz";
    private static final String dstFilePath = "layer.geojson.gz";
    private static final Logger logger = LoggerFactory.getLogger(MapLayerService.class.getName());

    private static final double TOLERANCE = 0.005;
    private static final int DECIMALS = 7;

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
                logger.info("Disable geojson updating waiting queue.");
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
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(new FileOutputStream(tmpFilePath));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzipOutputStream))) {
            // Start the FeatureCollection JSON structure
            writer.write("{\"type\": \"FeatureCollection\", \"features\": [");

            int offset = 0;
            int limit = 1000;
            boolean firstFeature = true;

            logger.info("Query trails from the database.");
            List<Trail> trails = this.iTrailRepository.fetchTrails(offset, limit);
            logger.info("Queried {} trails", trails.size());

            while (!trails.isEmpty()) {
                for (Trail trail : trails) {
                    // Only add a comma if it's not the first feature
                    if (!firstFeature) {
                        writer.write(",");
                    }

                    // Convert trail to feature JSON
                    String featureJson = convertLineStringToFeature(trail.getCoordinates(), trail.getTrailname(), trail.getId());

                    // Debug output to ensure JSON is correct
                    logger.debug("Generated feature JSON: " + featureJson);

                    writer.write(featureJson);  // Write the JSON feature to file
                    firstFeature = false;  // Set to false after the first feature
                }
                offset += limit;
                trails = this.iTrailRepository.fetchTrails(offset, limit);
            }

            // Close the FeatureCollection
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
    private String convertLineStringToFeature(byte[] rawLine, String trailName, Long id) throws IOException, ParseException {
        LineString preToleranceLine = (LineString) this.wkbReader.read(rawLine);

        // Size reduction algorithms
        LineString toleranceLine = simplifyLineString(preToleranceLine);
        LineString precisionLine = reducePrecision(toleranceLine);

        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        // Create geometry part of GeoJSON
        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "LineString");

        // Convert LineString coordinates to GeoJSON format
        double[][] coordinates = new double[precisionLine.getCoordinates().length][2];
        for (int j = 0; j < precisionLine.getCoordinates().length; j++) {
            coordinates[j][0] = precisionLine.getCoordinateN(j).x;
            coordinates[j][1] = precisionLine.getCoordinateN(j).y;
        }
        geometry.put("coordinates", coordinates);
        feature.put("geometry", geometry);

        // Add properties (e.g., title)
        Map<String, Object> properties = new HashMap<>();
        properties.put("trail_name", trailName);
        properties.put("id", id);
        feature.put("properties", properties);

        // Convert feature map to JSON string using Jackson
        return objectMapper.writeValueAsString(feature);
    }

    private LineString simplifyLineString(LineString line) {
        // Simplify LineString using Douglas-Peucker
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(line);
        simplifier.setDistanceTolerance(TOLERANCE);
        return (LineString) simplifier.getResultGeometry();
    }

    private LineString reducePrecision(LineString line) {
        Coordinate[] originalCoordinates = line.getCoordinates();
        Coordinate[] reducedCoordinates = new Coordinate[originalCoordinates.length];

        double scale = Math.pow(10, DECIMALS);
        for (int i = 0; i < originalCoordinates.length; i++) {
            double x = Math.round(originalCoordinates[i].x * scale) / scale;
            double y = Math.round(originalCoordinates[i].y * scale) / scale;
            reducedCoordinates[i] = new Coordinate(x, y);
        }

        return line.getFactory().createLineString(reducedCoordinates);
    }
}

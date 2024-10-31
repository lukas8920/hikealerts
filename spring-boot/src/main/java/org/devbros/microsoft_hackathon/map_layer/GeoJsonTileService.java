package org.devbros.microsoft_hackathon.map_layer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeoJsonTileService {
    public String generateTile(String geoJson, int z, int x, int y) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(geoJson);
            double[] bounds = getTileBounds(z, x, y);

            double minLon = bounds[0];
            double minLat = bounds[1];
            double maxLon = bounds[2];
            double maxLat = bounds[3];

            // Create a new GeoJSON feature collection to hold the filtered features
            ArrayNode filteredFeatures = objectMapper.createArrayNode();

            for (JsonNode feature : rootNode.path("features")) {
                JsonNode geometry = feature.path("geometry");
                // You need to implement a method to check if the geometry falls within the bounds
                if (isFeatureInBounds(geometry, minLon, minLat, maxLon, maxLat)) {
                    filteredFeatures.add(feature);
                }
            }

            ObjectNode tileGeoJson = objectMapper.createObjectNode();
            tileGeoJson.set("type", objectMapper.convertValue("FeatureCollection", JsonNode.class));
            tileGeoJson.set("features", filteredFeatures);

            return objectMapper.writeValueAsString(tileGeoJson);
        } catch (IOException e) {
            e.printStackTrace();
            return "{}"; // Return an empty GeoJSON object on error
        }
    }

    private double[] getTileBounds(int z, int x, int y) {
        // Calculate the geographical bounds of the tile
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        double lon1 = (x / Math.pow(2.0, z) * 360.0) - 180.0;
        double lat1 = Math.toDegrees(Math.atan(Math.sinh(n)));
        double lon2 = ((x + 1) / Math.pow(2.0, z) * 360.0) - 180.0;
        double lat2 = Math.toDegrees(Math.atan(Math.sinh(n)));

        return new double[]{lon1, lat1, lon2, lat2}; // [minLon, minLat, maxLon, maxLat]
    }

    private boolean isFeatureInBounds(JsonNode geometry, double minLon, double minLat, double maxLon, double maxLat) {
        String type = geometry.path("type").asText();

        return switch (type) {
            case "Point" -> isPointInBounds(geometry, minLon, minLat, maxLon, maxLat);
            case "LineString" -> isLineStringInBounds(geometry, minLon, minLat, maxLon, maxLat);
            case "Polygon" -> isPolygonInBounds(geometry, minLon, minLat, maxLon, maxLat);
            default -> false; // Handle other geometry types as needed
        };
    }

    private boolean isPointInBounds(JsonNode point, double minLon, double minLat, double maxLon, double maxLat) {
        double lon = point.path("coordinates").get(0).asDouble();
        double lat = point.path("coordinates").get(1).asDouble();
        return lon >= minLon && lon <= maxLon && lat >= minLat && lat <= maxLat;
    }

    private boolean isLineStringInBounds(JsonNode lineString, double minLon, double minLat, double maxLon, double maxLat) {
        for (JsonNode coordinate : lineString.path("coordinates")) {
            double lon = coordinate.get(0).asDouble();
            double lat = coordinate.get(1).asDouble();
            if (lon >= minLon && lon <= maxLon && lat >= minLat && lat <= maxLat) {
                return true; // At least one point in bounds
            }
        }
        return false; // No points in bounds
    }

    private boolean isPolygonInBounds(JsonNode polygon, double minLon, double minLat, double maxLon, double maxLat) {
        for (JsonNode ring : polygon.path("coordinates")) {
            for (JsonNode coordinate : ring) {
                double lon = coordinate.get(0).asDouble();
                double lat = coordinate.get(1).asDouble();
                if (lon >= minLon && lon <= maxLon && lat >= minLat && lat <= maxLat) {
                    return true; // At least one point in bounds
                }
            }
        }
        return false; // No points in bounds
    }
}

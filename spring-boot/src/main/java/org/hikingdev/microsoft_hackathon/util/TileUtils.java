package org.hikingdev.microsoft_hackathon.util;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.adapt.jts.*;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerBuild;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerParams;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.PbfTile;
import org.hikingdev.microsoft_hackathon.map_layer.SpatialItem;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class TileUtils {
    private static final Logger logger = LoggerFactory.getLogger(TileUtils.class);

    public static byte[] generateTile(List<SpatialItem> trails, double[] bbox){
        IGeometryFilter acceptAllGeomFilter = geometry -> geometry instanceof LineString;
        Envelope tileEnvelope = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        MvtLayerParams layerParams = new MvtLayerParams(); // Default extent
        GeometryFactory geometryFactory = new GeometryFactory();

        VectorTile.Tile.Layer.Builder layerBuilder = MvtLayerBuild.newLayerBuilder("trail_layer", layerParams);
        VectorTile.Tile.Builder tileBuilder = VectorTile.Tile.newBuilder();

        trails.forEach(trail -> {
            logger.debug("Create tile for {}", trail.getTrailname());
            logger.debug("... in bbox {}, {}, {}, {}", bbox[0], bbox[2], bbox[1], bbox[3]);
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", trail.getTrailname());
            properties.put("id", trail.getId());

            TileGeomResult tileGeom = JtsAdapter.createTileGeom(
                    trail.getLineString(),
                    tileEnvelope,
                    geometryFactory,
                    layerParams,
                    acceptAllGeomFilter);

            MvtLayerProps layerProps = new MvtLayerProps();
            IUserDataConverter userDataConverter = new UserDataKeyValueMapConverter();
            List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(tileGeom.mvtGeoms, layerProps, userDataConverter);
            if (features.size() > 0){
                VectorTile.Tile.Feature feature = addPropertiesToFeature(features.get(0), layerBuilder, properties);
                layerBuilder.addAllFeatures(List.of(feature));
            }
        });
        // indicate that trails should be cleared by garbage collector
        trails = null;

        tileBuilder.addLayers(layerBuilder.build());
        VectorTile.Tile mvt = tileBuilder.build();
        return mvt.toByteArray();
    }

    static VectorTile.Tile.Feature addPropertiesToFeature(VectorTile.Tile.Feature feature,
                                                   VectorTile.Tile.Layer.Builder layerBuilder,
                                                   Map<String, Object> properties) {
        VectorTile.Tile.Feature.Builder featureBuilder = feature.toBuilder();

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Find or add key index
            int keyIndex = layerBuilder.getKeysList().indexOf(key);
            if (keyIndex == -1) {
                layerBuilder.addKeys(key);
                keyIndex = layerBuilder.getKeysList().size() - 1;
            }

            // Find or add value index
            int valueIndex = -1;
            for (int i = 0; i < layerBuilder.getValuesList().size(); i++) {
                if (layerBuilder.getValuesList().get(i).getStringValue().equals(value)) {
                    valueIndex = i;
                    break;
                }
            }
            if (valueIndex == -1) {
                VectorTile.Tile.Value.Builder builder = VectorTile.Tile.Value.newBuilder();
                if (value instanceof String){
                    builder.setStringValue((String) value);
                } else if (value instanceof Long){
                    builder.setIntValue((Long) value);
                }
                VectorTile.Tile.Value newValue = builder.build();
                layerBuilder.addValues(newValue);
                valueIndex = layerBuilder.getValuesList().size() - 1;
            }

            // Add the new key-value pair to the feature's tags
            featureBuilder.addTags(keyIndex);
            featureBuilder.addTags(valueIndex);
        }

        return featureBuilder.build();
    }

    public static LineString transformLineString(Geometry geometry){
        List<Coordinate> transformedCoords = new ArrayList<>();
        for (Coordinate coord : geometry.getCoordinates()) {
            Coordinate transformedCoordinate = transformCoordinate(coord);
            transformedCoords.add(transformedCoordinate);
        }
        return new GeometryFactory().createLineString(transformedCoords.toArray(new Coordinate[0]));
    }

    public static Coordinate transformCoordinate(Coordinate coordinate){
        double x;
        double y;
        x = lonToMerc(coordinate.x);
        y = latToMerc(coordinate.y);
        return new Coordinate(x, y);
    }

    public static double[] transformBbox(double[] bbox){
        return new double[]{
          lonToMerc(bbox[0]),
          latToMerc(bbox[1]),
          lonToMerc(bbox[2]),
          latToMerc(bbox[3])
        };
    }

    public static double[] tileToBBox(int x, int y, int z) {
        // Number of tiles per zoom level
        double n = Math.pow(2, z);

        // Longitude range: from -180 to 180
        double lon_w = (x / n) * 360.0 - 180;
        double lon_e = ((x + 1) / n) * 360.0 - 180;

        // Latitude range: use Mercator projection
        double lat_n = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n))));
        double lat_s = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * (y + 1) / n))));

        // Return the bounding box as [lon_w, lat_s, lon_e, lat_n]
        return new double[] { lon_w, lat_s, lon_e, lat_n };
    }

    // Convert longitude to EPSG:3857
    private static double lonToMerc(double lon) {
        return lon * 20037508.34 / 180;
    }

    // Convert latitude to EPSG:3857
    private static double latToMerc(double lat) {
        double rad = Math.toRadians(lat);
        return Math.log(Math.tan(Math.PI / 4 + rad / 2)) * 6378137;
    }

    public static Set<PbfTile> getIntersectedTiles(LineString lineString, int zoomMin, int zoomMax) {
        logger.info("Number of coords {}", lineString.getCoordinates().length);
        Set<PbfTile> intersectedTiles = ConcurrentHashMap.newKeySet(); // Thread-safe set for parallel processing

        // Process each zoom level in parallel
        IntStream.rangeClosed(zoomMin, zoomMax).parallel().forEach(zoom -> {
            List<PbfTile> pbfTiles = new ArrayList<>();
            for (int i = 0; i < lineString.getNumPoints() - 1; i++) {
                Coordinate start = lineString.getCoordinateN(i);
                Coordinate end = lineString.getCoordinateN(i + 1);

                // Skip zero-length segments (improves performance)
                if (start.equals(end)) continue;

                // Convert to tile coordinates
                int[] startTile = latLonToTileXY(start.y, start.x, zoom);
                int[] endTile = latLonToTileXY(end.y, end.x, zoom);

                // Avoid re-adding identical tiles
                if (!startTile.equals(endTile)) {
                    pbfTiles.addAll(getLineTiles(startTile, endTile, zoom));
                }
            }
            intersectedTiles.addAll(new HashSet<>(pbfTiles));
        });
        return intersectedTiles;
    }

    // Convert latitude and longitude to tile x, y coordinates at zoom level
    private static int[] latLonToTileXY(double lat, double lon, int zoom) {
        double n = Math.pow(2, zoom);
        int x = (int) ((lon + 180.0) / 360.0 * n);
        int y = (int) ((1.0 - Math.log(Math.tan(Math.toRadians(lat)) + 1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * n);
        return new int[]{x, y};
    }

    // Calculate tiles along the line segment (using a Bresenham-like approach)
    private static Set<PbfTile> getLineTiles(int[] start, int[] end, int zoom) {
        Set<PbfTile> tiles = new HashSet<>();
        int x0 = start[0], y0 = start[1];
        int x1 = end[0], y1 = end[1];

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            tiles.add(new PbfTile(x0, y0, zoom));
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 < dx) { err += dx; y0 += sy; }
        }
        return tiles;
    }
}

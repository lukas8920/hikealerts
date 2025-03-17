package org.hikingdev.microsoft_hackathon.map_layer;

import org.hikingdev.microsoft_hackathon.map_layer.entities.SpatialItem;
import org.hikingdev.microsoft_hackathon.util.geodata.TileUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class TileGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TileGenerator.class);

    private static final double TOLERANCE = 0.002;

    private final STRtree spatialIndex;

    public TileGenerator(List<SpatialItem> spatialItems) {
        spatialIndex = new STRtree();

        // Build spatial index
        for (SpatialItem spatialItem : spatialItems) {
            spatialItem.setLineString(simplifyLineString((LineString) spatialItem.getLineString()));
            spatialItem.setLineString(TileUtils.transformLineString(spatialItem.getLineString()));
            Envelope envelope = spatialItem.getLineString().getEnvelopeInternal();
            spatialIndex.insert(envelope, spatialItem);
        }
    }

    // Get line strings that intersect with a specific tile
    public List<SpatialItem> getLineStringsForTile(double[] bbox) {
        Envelope tileBoundingBox = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
        return spatialIndex.query(tileBoundingBox);
    }

    private LineString simplifyLineString(LineString line) {
        // Simplify LineString using Douglas-Peucker
        DouglasPeuckerSimplifier simplifier = new DouglasPeuckerSimplifier(line);
        simplifier.setDistanceTolerance(TOLERANCE);
        return (LineString) simplifier.getResultGeometry();
    }

    // Generate a vector tile based on line strings
    public Optional<byte[]> generateTile(int x, int y, int zoom) {
        double[] bbox = TileUtils.transformBbox(TileUtils.tileToBBox(x, y, zoom));

        List<SpatialItem> geometriesInTile = getLineStringsForTile(bbox);
        if (geometriesInTile.isEmpty()){
            return Optional.empty();
        }
        logger.debug("Generate tile {}, {}, {}", x, y, zoom);
        return Optional.of(TileUtils.generateTile(geometriesInTile, bbox));
    }
}

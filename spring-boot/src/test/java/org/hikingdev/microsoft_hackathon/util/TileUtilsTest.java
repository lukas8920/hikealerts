package org.hikingdev.microsoft_hackathon.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.wdtinc.mapbox_vector_tile.VectorTile;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.PbfTile;
import org.hikingdev.microsoft_hackathon.map_layer.entities.SpatialItem;
import org.hikingdev.microsoft_hackathon.util.geodata.TileUtils;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TileUtilsTest {
    @Test
    public void testThatGenerateTileContainsLineStrings() throws InvalidProtocolBufferException {
        GeometryFactory geometryFactory = new GeometryFactory();
        double[] bbox = new double[]{0.0, 0.0, 10.0, 10.0};
        // LineString 1: partially inside
        LineString line1 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(-5, 5),   // Outside bbox
                new Coordinate(5, 5),    // Inside bbox
                new Coordinate(15, 5)    // Outside bbox
        });
        // LineString 2: another line partially inside
        LineString line2 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(2, -2),   // Outside bbox
                new Coordinate(5, 5),    // Inside bbox
                new Coordinate(8, 12)    // Outside bbox
        });

        SpatialItem spatialItem1 = new SpatialItem(null, null, line1);
        SpatialItem spatialItem2 = new SpatialItem(null, null, line2);
        List<SpatialItem> spatialItems = List.of(spatialItem1, spatialItem2);

        byte[] bytes = TileUtils.generateTile(spatialItems, bbox);

        VectorTile.Tile tile = VectorTile.Tile.parseFrom(bytes);
        assertThat(tile.getLayers(0).getFeaturesCount(), is(2));
    }

    @Test
    public void testThatGenerateTileIgnoresTrailsOutsideBbox() throws InvalidProtocolBufferException {
        GeometryFactory geometryFactory = new GeometryFactory();
        double[] bbox = new double[]{0.0, 0.0, 10.0, 10.0};
        // LineString 1: partially inside
        LineString line1 = geometryFactory.createLineString(new Coordinate[]{
                new Coordinate(12, 12),   // Outside bbox
                new Coordinate(14, 14),    // Inside bbox
                new Coordinate(20, 20)    // Outside bbox
        });
        SpatialItem spatialItem = new SpatialItem(null, null, line1);
        List<SpatialItem> spatialItems = List.of(spatialItem);

        byte[] bytes = TileUtils.generateTile(spatialItems, bbox);

        VectorTile.Tile tile = VectorTile.Tile.parseFrom(bytes);
        assertThat(tile.getLayers(0).getFeaturesCount(), is(0));
    }

    @Test
    public void testThatGenerateTileReturnsEmptyTile() throws InvalidProtocolBufferException {
        double[] bbox = new double[]{0.0, 0.0, 10.0, 10.0};

        List<SpatialItem> spatialItems = new ArrayList<>();

        byte[] bytes = TileUtils.generateTile(spatialItems, bbox);

        VectorTile.Tile tile = VectorTile.Tile.parseFrom(bytes);
        assertThat(tile.getLayers(0).getFeaturesCount(), is(0));
    }

    @Test
    public void testTileToBBox() {
        // Test cases with known inputs and expected outputs
        // Test 1: Top-left tile at zoom level 0 (the whole world)
        int x1 = 0, y1 = 0, z1 = 0;
        double[] bbox1 = TileUtils.tileToBBox(x1, y1, z1);

        double[] expectedBBox1 = {-180.0, -85.0511287798066, 180.0, 85.0511287798066};
        assertThat(bbox1, is(expectedBBox1));

        // Test 2: Tile (1, 1) at zoom level 1
        int x2 = 1, y2 = 1, z2 = 1;
        double[] bbox2 = TileUtils.tileToBBox(x2, y2, z2);

        double[] expectedBBox2 = {0.0, -85.0511287798066, 180.0, 0.0};
        assertThat(bbox2, is(expectedBBox2));

        // Test 3: Tile (0, 1) at zoom level 1
        int x3 = 0, y3 = 1, z3 = 1;
        double[] bbox3 = TileUtils.tileToBBox(x3, y3, z3);

        double[] expectedBBox3 = {-180.0, -85.0511287798066, 0.0, 0.0};
        assertThat(bbox3, is(expectedBBox3));

        // Test 4: Tile (2, 2) at zoom level 4
        int x4 = 3, y4 = 2, z4 = 4;
        double[] bbox4 = TileUtils.tileToBBox(x4, y4, z4);

        double[] expectedBBox4 = {-112.5, 74.01954331150226, -90.0, 79.17133464081945};
        assertThat(bbox4, is(expectedBBox4));

        // Test 4: Tile (2, 2) at zoom level 5
        int x5 = 5, y5 = 3, z5 = 5;
        double[] bbox5 = TileUtils.tileToBBox(x5, y5, z5);

        double[] expectedBBox5 = {-123.75, 79.17133464081945, -112.5, 81.09321385260839};
        assertThat(bbox5, is(expectedBBox5));
    }

    @Test
    public void testTransformCoordinate(){
        Coordinate coordinate1 = new Coordinate(-74.006, 40.7128);

        Coordinate transformedCoordinate1 = TileUtils.transformCoordinate(coordinate1);

        assertThat(transformedCoordinate1.x, is(-8238310.234500223));
        assertThat(transformedCoordinate1.y, is(4970071.579142425));

        Coordinate coordinate2 = new Coordinate(90, 0);

        Coordinate transformedCoordinate2 = TileUtils.transformCoordinate(coordinate2);

        assertThat(transformedCoordinate2.x, is(1.001875417E7));
        assertThat(transformedCoordinate2.y, is(-7.081154551613622E-10));

        Coordinate coordinate3 = new Coordinate(0, 51.5074);

        Coordinate transformedCoordinate3 = TileUtils.transformCoordinate(coordinate3);

        assertThat(transformedCoordinate3.x, is(0.0));
        assertThat(transformedCoordinate3.y, is(6711542.475587636));
    }

    @Test
    public void testIntersectedTilesShortLine(){
        LineString line = new GeometryFactory().createLineString(new Coordinate[]{
                new Coordinate(-122.4194, 37.7749),  // San Francisco
                new Coordinate(-122.2711, 37.8044)   // Oakland
        });
        int zoomMin = 10, zoomMax = 10;

        PbfTile expectedTile1 = new PbfTile(163, 395, 10);
        PbfTile expectedTile2 = new PbfTile(164, 395, 10);

        Set<PbfTile> pbfTiles = TileUtils.getIntersectedTiles(line, zoomMin, zoomMax);

        assertThat(pbfTiles.size(), is(2));
        assertThat(pbfTiles.contains(expectedTile1), is(true));
        assertThat(pbfTiles.contains(expectedTile2), is(true));
    }

    @Test
    public void testIntersectedTilesHorizontalLine(){
        LineString line = new GeometryFactory().createLineString(new Coordinate[]{
                new Coordinate(2.3522, 48.8566),   // Paris
                new Coordinate(3.3522, 48.8566)    // Slightly to the east of Paris
        });
        int zoomMin = 12, zoomMax = 12;

        PbfTile expectedTile1 = new PbfTile(2074, 1409, 12);
        PbfTile expectedTile2 = new PbfTile(2075, 1409, 12);
        PbfTile expectedTile3 = new PbfTile(2076, 1409, 12);
        PbfTile expectedTile4 = new PbfTile(2077, 1409, 12);
        PbfTile expectedTile5 = new PbfTile(2078, 1409, 12);

        Set<PbfTile> pbfTiles = TileUtils.getIntersectedTiles(line, zoomMin, zoomMax);

        assertThat(pbfTiles.size(), is(13));
        assertThat(pbfTiles.contains(expectedTile1), is(true));
        assertThat(pbfTiles.contains(expectedTile2), is(true));
        assertThat(pbfTiles.contains(expectedTile3), is(true));
        assertThat(pbfTiles.contains(expectedTile4), is(true));
        assertThat(pbfTiles.contains(expectedTile5), is(true));
    }
}
package org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKBWriter;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class GeoMatcherTest {
    private static Polygon polygon;
    private static GeoMatcher geoMatcher;

    @BeforeAll
    public static void setup(){
        geoMatcher = new GeoMatcher();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 5), new Coordinate(5, 5), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        polygon = new Polygon(linearRing, new LinearRing[]{}, geometryFactory);
    }

    @Test
    public void testLinestringInPolygon(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(2, 8), new Coordinate(2, 4)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LineString lineString = new LineString(coordinateSequence, geometryFactory);
        WKBWriter wkbWriter = new WKBWriter();
        Trail trail = new Trail();
        trail.setCoordinates(wkbWriter.write(lineString));

        List<Trail> trails = geoMatcher.match(polygon, trail);

        assertThat(trails.size(), is(1));
    }

    @Test
    public void testInvalidLinestring(){
        byte[] bytes = new byte[]{'h', 'd', 'i'};
        Trail trail = new Trail();
        trail.setCoordinates(bytes);

        List<Trail> trails = geoMatcher.match(polygon, trail);

        assertThat(trails.size(), is(0));
    }

    @Test
    public void testLinestringOutsidePolygon(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(2, 8), new Coordinate(2, 6)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LineString lineString = new LineString(coordinateSequence, geometryFactory);
        WKBWriter wkbWriter = new WKBWriter();
        Trail trail = new Trail();
        trail.setCoordinates(wkbWriter.write(lineString));

        List<Trail> trails = geoMatcher.match(polygon, trail);

        assertThat(trails.size(), is(0));
    }
}

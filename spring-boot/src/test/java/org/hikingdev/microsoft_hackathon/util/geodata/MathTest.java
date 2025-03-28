package org.hikingdev.microsoft_hackathon.util.geodata;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class MathTest {
    @Test
    public void testTransformToEPSG3857Coordinate(){
        Coordinate coordinate1 = new Coordinate(-74.006, 40.7128);

        Coordinate transformedCoordinate1 = Math.convertCoordinateToEPSG3857(coordinate1);

        assertThat(transformedCoordinate1.x, is(-8238310.234500223));
        assertThat(transformedCoordinate1.y, is(4970071.579142425));

        Coordinate coordinate2 = new Coordinate(90, 0);

        Coordinate transformedCoordinate2 = Math.convertCoordinateToEPSG3857(coordinate2);

        assertThat(transformedCoordinate2.x, is(1.001875417E7));
        assertThat(transformedCoordinate2.y, is(-7.081154551613622E-10));

        Coordinate coordinate3 = new Coordinate(0, 51.5074);

        Coordinate transformedCoordinate3 = Math.convertCoordinateToEPSG3857(coordinate3);

        assertThat(transformedCoordinate3.x, is(0.0));
        assertThat(transformedCoordinate3.y, is(6711542.475587636));
    }

    @Test
    public void testTransformToEPSG4326Coordinate(){
        Coordinate coordinate1 = new Coordinate(-8238310.234500223, 4970071.579142425);

        Coordinate transformedCoordinate1 = Math.convertCoordinateToWGS84(coordinate1);

        assertThat(transformedCoordinate1.x, closeTo(-74.006, 1e-6));
        assertThat(transformedCoordinate1.y, closeTo(40.7128, 1e-6));

        Coordinate coordinate2 = new Coordinate(1.001875417E7, -7.081154551613622E-10);

        Coordinate transformedCoordinate2 = Math.convertCoordinateToWGS84(coordinate2);

        assertThat(transformedCoordinate2.x, is(90.0));
        assertThat(transformedCoordinate2.y, is(0.0));

        Coordinate coordinate3 = new Coordinate(0, 6711542.475587636);

        Coordinate transformedCoordinate3 = Math.convertCoordinateToWGS84(coordinate3);

        assertThat(transformedCoordinate3.x, is(0.0));
        assertThat(transformedCoordinate3.y, closeTo(51.5074, 1e-6));
    }

    @Test
    public void testInvalidWGS84Linestring(){
        LineString lineString = epsg3857LineString();

        boolean result = Math.isValidWGS84(lineString);

        assertThat(result, is(false));
    }

    @Test
    public void testValidWGS84Linestring(){
        LineString lineString = wgs84LineString();

        boolean result = Math.isValidWGS84(lineString);

        assertThat(result, is(true));
    }

    @Test
    public void testValidEPSG3857Linestring(){
        LineString lineString = epsg3857LineString();

        boolean result = Math.isValidEPSG3857(lineString);

        assertThat(result, is(true));
    }

    private LineString wgs84LineString(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(-74.006, 40.7128),
                new Coordinate(90, 0),
                new Coordinate(0, 51.5074)
        };
        CoordinateSequence seq = new CoordinateArraySequence(coordinates);
        return new LineString(seq, geometryFactory);
    }

    private LineString epsg3857LineString(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(-8238310.234500223, 4970071.579142425),
                new Coordinate(1.001875417E7, -7.081154551613622E-10),
                new Coordinate(0.0, 6711542.475587636)
        };
        CoordinateSequence seq = new CoordinateArraySequence(coordinates);
        return new LineString(seq, geometryFactory);
    }
}

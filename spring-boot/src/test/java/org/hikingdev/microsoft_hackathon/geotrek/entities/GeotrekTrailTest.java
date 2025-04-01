package org.hikingdev.microsoft_hackathon.geotrek.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class GeotrekTrailTest {
    private final GeometryFactory geometryFactory = new GeometryFactory();

    private GeotrekTrail geotrekTrail1;
    private GeotrekTrail geotrekTrail2;
    private GeotrekTrail geotrekTrail3;

    @BeforeEach
    public void setup(){
        Coordinate[] coords1 = new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 1)
        };
        CoordinateSequence seq1 = new CoordinateArraySequence(coords1);
        LineString line1 = new LineString(seq1, geometryFactory);
        geotrekTrail1 = new GeotrekTrail();
        geotrekTrail1.setId("1");
        geotrekTrail1.setCoordinates(line1);
        geotrekTrail1.setSource(4);
        geotrekTrail1.setTarget(5);

        Coordinate[] coords2 = new Coordinate[]{
                new Coordinate(2, 2),
                new Coordinate(3, 3)
        };
        CoordinateSequence seq2 = new CoordinateArraySequence(coords2);
        LineString line2 = new LineString(seq2, geometryFactory);
        geotrekTrail2 = new GeotrekTrail();
        geotrekTrail2.setId("2");
        geotrekTrail2.setCoordinates(line2);
        geotrekTrail2.setSource(5);
        geotrekTrail2.setTarget(6);

        Coordinate[] coords3 = new Coordinate[]{
                new Coordinate(4, 4),
                new Coordinate(5, 5)
        };
        CoordinateSequence seq3 = new CoordinateArraySequence(coords3);
        LineString line3 = new LineString(seq3, geometryFactory);
        geotrekTrail3 = new GeotrekTrail();
        geotrekTrail3.setId("3");
        geotrekTrail3.setCoordinates(line3);
        geotrekTrail3.setSource(6);
        geotrekTrail3.setTarget(7);
    }

    @Test
    public void testReverseTrailsAreJoinedCorrectly(){
        List<GeotrekTrail> geotrekTrails = List.of(geotrekTrail3, geotrekTrail2, geotrekTrail1);

        GeotrekTrail outputGeotrail = GeotrekTrail.joinGeotrekTrails(geotrekTrails);

        Coordinate[] outputCoords = new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 2),
                new Coordinate(3, 3),
                new Coordinate(4, 4),
                new Coordinate(5, 5)
        };

        assertThat(outputGeotrail.getCoordinates().getNumPoints(), is(6));
        assertThat(outputGeotrail.getCoordinates().getCoordinates(), is(outputCoords));
        assertThat(outputGeotrail.getId(), is("1"));
    }

    @Test
    public void testLoopedTrailsAreJoinedCorrectly(){
        geotrekTrail3.setSource(6);
        geotrekTrail3.setTarget(4);

        List<GeotrekTrail> geotrekTrails = List.of(geotrekTrail3, geotrekTrail2, geotrekTrail1);

        GeotrekTrail outputGeotrail = GeotrekTrail.joinGeotrekTrails(geotrekTrails);

        Coordinate[] outputCoords = new Coordinate[]{
                new Coordinate(4, 4),
                new Coordinate(5, 5),
                new Coordinate(0, 0),
                new Coordinate(1, 1),
                new Coordinate(2, 2),
                new Coordinate(3, 3)
        };

        assertThat(outputGeotrail.getCoordinates().getNumPoints(), is(6));
        assertThat(outputGeotrail.getCoordinates().getCoordinates(), is(outputCoords));
        assertThat(outputGeotrail.getId(), is("3"));
    }
}

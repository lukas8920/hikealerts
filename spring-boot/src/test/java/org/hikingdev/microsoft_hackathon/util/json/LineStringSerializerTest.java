package org.hikingdev.microsoft_hackathon.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class LineStringSerializerTest {
    @Test
    void testSerializeValidLineString() throws JsonProcessingException {
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(4708222.559304178, 259961.13886931736),
                new Coordinate(4708287.053046793, 259921.72602660747),
                new Coordinate(4708396.931274952, 259926.50334087657)
        };
        CoordinateSequence seq = new CoordinateArraySequence(coordinates);
        LineString lineString = new LineString(seq, geometryFactory);
        SerializationMapper serializationMapper = new SerializationMapper();

        String json = serializationMapper.writeValueAsString(lineString);

        assertThat(json, is("[[4708222.559304178,259961.13886931736],[4708287.053046793,259921.72602660747],[4708396.931274952,259926.50334087657]]"));
    }
}

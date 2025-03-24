package org.hikingdev.microsoft_hackathon.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LineStringDeserializerTest {
    @Test
    void testDeserializeValidLineString() throws Exception {
            String json = "{ \"type\": \"LineString\", \"coordinates\": [ " +
                    "[4708222.559304178, 259961.13886931736], " +
                    "[4708287.053046793, 259921.72602660747], " +
                    "[4708396.931274952, 259926.50334087657] ] }";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                .addDeserializer(LineString.class, new LineStringDeserializer()));

        LineString lineString = objectMapper.readValue(json, LineString.class);

        assertThat(lineString.getNumPoints(), is(3));

        Coordinate[] coordinates = lineString.getCoordinates();
        assertThat(coordinates[0].x, is(4708222.559304178));
        assertThat(coordinates[0].y, is(259961.13886931736));

        assertThat(coordinates[1].x, is(4708287.053046793));
        assertThat(coordinates[1].y, is(259921.72602660747));

        assertThat(coordinates[2].x, is(4708396.931274952));
        assertThat(coordinates[2].y, is(259926.50334087657));
    }

    @Test
    void testDeserializeInvalidJson() {
        String invalidJson = "{ \"type\": \"LineString\" }";

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                .addDeserializer(LineString.class, new LineStringDeserializer()));

        Exception exception = assertThrows(IOException.class, () -> {
            objectMapper.readValue(invalidJson, LineString.class);
        });

        assertThat(exception.getMessage(), is("Unexpected IOException (of type java.io.IOException): Invalid LineString JSON"));
    }
}

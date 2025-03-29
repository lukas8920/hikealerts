package org.hikingdev.microsoft_hackathon.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;

public class LineStringSerializer extends JsonSerializer<LineString> {
    @Override
    public void serialize(LineString lineString, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();
        for (Coordinate coord : lineString.getCoordinates()) {
            generator.writeStartArray();
            generator.writeNumber(coord.getX());
            generator.writeNumber(coord.getY());
            generator.writeEndArray();
        }
        generator.writeEndArray();
    }
}

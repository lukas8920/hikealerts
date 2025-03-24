package org.hikingdev.microsoft_hackathon.util.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineStringDeserializer extends JsonDeserializer<LineString> {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public LineString deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (!node.has("coordinates") || !node.get("coordinates").isArray()) {
            throw new IOException("Invalid LineString JSON");
        }

        List<Coordinate> coordinates = new ArrayList<>();
        for (JsonNode coordNode : node.get("coordinates")) {
            double x = coordNode.get(0).asDouble();
            double y = coordNode.get(1).asDouble();
            coordinates.add(new Coordinate(x, y));
        }

        return geometryFactory.createLineString(coordinates.toArray(new Coordinate[0]));
    }
}

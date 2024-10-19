package org.devbros.microsoft_hackathon.event_handling.event_injection.matcher;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeoMatcher {
    private static final Logger logger = LoggerFactory.getLogger(GeoMatcher.class.getName());

    private final GeometryFactory geometryFactory;
    private final WKBReader wkbReader;

    public GeoMatcher(){
        this.wkbReader = new WKBReader();
        this.geometryFactory = new GeometryFactory();
    }

    public List<Trail> match(Polygon polygon, Trail trail) {
        List<Trail> trails = new ArrayList<>();
        LineString lineString;
        try {
            lineString = (LineString) this.wkbReader.read(trail.getCoordinates());
        } catch (ParseException e) {
            return trails;
        }

        // Iterate over the points of the LineString
        for (Coordinate coord : lineString.getCoordinates()) {
            Point point = geometryFactory.createPoint(coord);
            // Check if the point lies within the polygon
            if (polygon.contains(point)) {
                trails.add(trail);
                break;
            }
        }
        return trails;
    }
}

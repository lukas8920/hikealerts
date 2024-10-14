package org.devbros.microsoft_hackathon.event_injection.matcher;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

@Component
public class GeoMatcher {
    public Trail match(Polygon polygon, Trail trail){
        //todo: still to implement
        return null;
    }
}

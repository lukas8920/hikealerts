package org.hikingdev.microsoft_hackathon.geotrek;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.stereotype.Component;

@Component
public class TrailMapper {
    WKBWriter wkbWriter = new WKBWriter();

    public Trail map(GeotrekTrail geotrekTrail){
        Trail trail = new Trail();
        trail.setTrailname(geotrekTrail.getName());
        trail.setMaintainer(geotrekTrail.getMaintainer());
        if (geotrekTrail.getId() == null || geotrekTrail.getId().startsWith("geotrek-")) {
            trail.setTrailId(geotrekTrail.getId());
        } else {
            trail.setTrailId("geotrek-" + geotrekTrail.getId());
        }
        if (geotrekTrail.getCoordinates() != null){
            trail.setCoordinates(this.wkbWriter.write(geotrekTrail.getCoordinates()));
        }
        return trail;
    }
}

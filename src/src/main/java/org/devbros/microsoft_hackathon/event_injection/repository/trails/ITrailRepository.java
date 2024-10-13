package org.devbros.microsoft_hackathon.event_injection.repository.trails;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

public interface ITrailRepository {
    Trail findTrailByUnitCodeAndCountry(String unitCode, String country);
    List<Trail> findTrailsInRegion(Polygon polygon);
}

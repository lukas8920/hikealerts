package org.devbros.microsoft_hackathon.repository.trails;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

public interface ITrailRepository {
    Trail searchTrailByNameUnitCodeAndCountry(String name, String unitCode, String country, double threshold, double levenshteinWeight);
    List<Trail> findTrailsInRegion(Polygon polygon, String country);
    List<Trail> findTrailsByNameCodeAndCountry(Polygon polygon, String country, String code);
    List<Trail> fetchTrails(int offset, int limit);
    List<Trail> findAllTrailsByIds(List<Long> ids);
    Trail searchTrailByNameAndCountry(String name, String country, double threshold, double levenshteinWeight);
}

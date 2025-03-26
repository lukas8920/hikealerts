package org.hikingdev.microsoft_hackathon.repository.trails;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.NameMatcher;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

public interface ITrailRepository {
    Trail searchTrailByNameUnitCodeAndCountry(String name, String unitCode, String country, NameMatcher<Trail> nameMatcher);
    List<Trail> findTrailsInRegion(Polygon polygon, String country);
    List<Trail> findTrailsByNameCodeAndCountry(Polygon polygon, String country, String code);
    List<Trail> fetchTrails(int offset, int limit);
    List<Trail> findAllTrailsByIds(List<Long> ids);
    Trail searchTrailByNameAndCountry(String name, String country, NameMatcher<Trail> nameMatcher);
    Trail findTrailByIdAndCountry(String trailId, String country);
    List<Trail> findTrailsByEventIdAndCountry(String eventId, String country);
    void save(Trail trail);
    void delete(String trailId, List<String> publishers);
}

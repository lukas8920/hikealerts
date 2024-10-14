package org.devbros.microsoft_hackathon.repository.trails;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.event_injection.matcher.GeoMatcher;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrailRepository implements ITrailRepository {
    private final ITrailJpaRepository iTrailJpaRepository;
    private final GeoMatcher geoMatcher;
    private final NameMatcher<Trail> nameMatcher;

    private int pageSize = 1000;

    //for testing purposes only
    protected void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }

    @Autowired
    public TrailRepository(ITrailJpaRepository iTrailJpaRepository, GeoMatcher geoMatcher, NameMatcher<Trail> nameMatcher){
        this.iTrailJpaRepository = iTrailJpaRepository;
        this.geoMatcher = geoMatcher;
        this.nameMatcher = nameMatcher;
    }

    @Override
    public Trail findTrailByNameUnitCodeAndCountry(String searchName, String unitCode, String country) {
        int pageNumber = 0;  // start from page 0

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Slice<Trail> slice;

        do {
            slice = this.iTrailJpaRepository.findAllByUnitcodeAndCountry(unitCode, country, pageable);
            slice.getContent().forEach(trail -> {
                // Process each entity
                this.nameMatcher.match(searchName, trail);
            });
            pageable = pageable.next();  // Move to the next page
        } while (slice.hasContent());


        return this.nameMatcher.getTopMatchingEntity();
    }

    @Override
    public List<Trail> findTrailsInRegion(Polygon polygon, String country) {
        int pageNumber = 0;  // start from page 0

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Slice<Trail> slice;

        List<Trail> trails = new ArrayList<>();
        do {
            slice = this.iTrailJpaRepository.findAllByCountry(country, pageable);
            slice.getContent().forEach(trail -> {
                // Process each entity
                trails.add(this.geoMatcher.match(polygon, trail));
            });
            pageable = pageable.next();  // Move to the next page
        } while (slice.hasContent());

        return trails;
    }
}

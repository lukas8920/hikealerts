package org.devbros.microsoft_hackathon.repository.trails;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.event_injection.matcher.GeoMatcher;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.devbros.microsoft_hackathon.util.Worker;
import org.locationtech.jts.geom.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

@Component
public class TrailRepository implements ITrailRepository {
    private static final Logger logger = LoggerFactory.getLogger(TrailRepository.class.getName());

    private final ITrailJpaRepository iTrailJpaRepository;
    private final GeoMatcher geoMatcher;
    private final NameMatcher<Trail> nameMatcher;

    @Autowired
    public TrailRepository(ITrailJpaRepository iTrailJpaRepository, GeoMatcher geoMatcher, NameMatcher<Trail> nameMatcher){
        this.iTrailJpaRepository = iTrailJpaRepository;
        this.geoMatcher = geoMatcher;
        this.nameMatcher = nameMatcher;
    }

    @Override
    public Trail findTrailByNameUnitCodeAndCountry(String searchName, String unitCode, String country) {
        long offset = 0;  // start from page 0

        List<Trail> slice = this.iTrailJpaRepository.findAllByUnitcodeAndCountry(unitCode, country, offset);

        do {
            slice.forEach(trail -> {
                // Process each entity
                this.nameMatcher.match(searchName, trail);
            });
            offset = slice.get((slice.size() - 1)).getId();  // Move to the next page
            slice = this.iTrailJpaRepository.findAllByUnitcodeAndCountry(unitCode, country, offset);
        } while (slice != null && !slice.isEmpty());


        return this.nameMatcher.getT();
    }

    @Override
    public List<Trail> findTrailsInRegion(Polygon polygon, String country) {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        Phaser phaser = new Phaser(1);
        logger.info("Try to identify trails in polygon for country: " + country);
        long offset = 0;  // start from page 0

        List<Trail> trails = new ArrayList<>();
        List<Trail> slice = this.iTrailJpaRepository.findAllByCountry(country, offset);

        do {
            logger.info("Next slice - size: " + slice.size());
            logger.info(String.valueOf(offset));

            phaser.register();
            // Process each entity
            List<Trail> finalSlice = slice;
            executorService.submit(new Worker(phaser, () -> {
                finalSlice.forEach(trail -> {
                    trails.addAll(this.geoMatcher.match(polygon, trail));
                });
            }));

            // Update pageable to the next page
            offset = slice.get((slice.size() - 1)).getId();
            slice = this.iTrailJpaRepository.findAllByCountry(country, offset);
        } while (slice != null && !slice.isEmpty());  // Continue while there's content

        phaser.arriveAndAwaitAdvance();
        logger.info("Identified number of trails: " + trails.size());

        return trails;
    }
}

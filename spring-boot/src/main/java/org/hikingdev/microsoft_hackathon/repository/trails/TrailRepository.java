package org.hikingdev.microsoft_hackathon.repository.trails;

import jakarta.persistence.EntityManager;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GeoMatcher;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.NameMatcher;
import org.hikingdev.microsoft_hackathon.util.threading.Worker;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
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
    private final EntityManager entityManager;
    private final GeoMatcher geoMatcher;

    @Autowired
    public TrailRepository(ITrailJpaRepository iTrailJpaRepository, GeoMatcher geoMatcher, EntityManager entityManager){
        this.entityManager = entityManager;
        this.iTrailJpaRepository = iTrailJpaRepository;
        this.geoMatcher = geoMatcher;
    }

    @Override
    public Trail searchTrailByNameUnitCodeAndCountry(String searchName, String unitCode, String country, NameMatcher<Trail> nameMatcher) {
        nameMatcher.resetNameMatcher();
        logger.info("Search trail by trail name, unit code and country: " + searchName + ", " + unitCode + ", " + country);
        logger.info("Name Matcher intial status - " + nameMatcher.getT() + " - " + nameMatcher.getMatchingScore());
        long offset = 0;  // start from page 0

        List<Trail> slice = this.iTrailJpaRepository.findAllByUnitcodeAndCountry(unitCode, country, offset);
        logger.info("Trails in region: " + slice.size());

        if (slice.size() > 0){
            do {
                slice.forEach(trail -> {
                    // Process each entity
                    nameMatcher.match(searchName, trail);
                });
                offset = slice.get((slice.size() - 1)).getId();  // Move to the next page
                slice = this.iTrailJpaRepository.findAllByUnitcodeAndCountry(unitCode, country, offset);
            } while (slice != null && !slice.isEmpty());
        }

        logger.info("Identified matching trail: " + nameMatcher.getT());
        return nameMatcher.getT();
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

    @Override
    public List<Trail> findTrailsByNameCodeAndCountry(Polygon polygon, String country, String code) {
        return this.iTrailJpaRepository.findAllByCountryAndUnitcode(country, code);
    }

    @Override
    public List<Trail> fetchTrails(int offset, int limit) {
        return this.iTrailJpaRepository.getTrailsAfterOffset(offset, limit);
    }

    @Override
    public List<Trail> findAllTrailsByIds(List<Long> ids){
        return this.iTrailJpaRepository.findAllByIds(ids);
    }

    @Override
    public Trail searchTrailByNameAndCountry(String name, String country, NameMatcher<Trail> nameMatcher) {
        nameMatcher.resetNameMatcher();
        logger.info("Search trail by trail name and country: " + name + ", " + country);
        logger.info("Name Matcher intial status - " + nameMatcher.getT() + " - " + nameMatcher.getMatchingScore());
        long offset = 0;  // start from page 0

        List<Trail> slice = this.iTrailJpaRepository.findAllByCountry(country, offset);
        logger.info("Trails in region: " + slice.size());

        if (slice.size() > 0){
            do {
                slice.forEach(trail -> {
                    // Process each entity
                    nameMatcher.match(name, trail);
                });
                offset = slice.get((slice.size() - 1)).getId();  // Move to the next page
                slice = this.iTrailJpaRepository.findAllByCountry(country, offset);
            } while (slice != null && !slice.isEmpty());
        }

        logger.info("Identified matching trail: " + nameMatcher.getT());
        return nameMatcher.getT();
    }

    @Override
    public Trail findTrailByIdAndCountry(String trailId, String country) {
        return this.iTrailJpaRepository.findByTrailIdAndCountry(trailId, country);
    }

    public List<Trail> findTrailsByEventIdAndCountry(String eventId, String country){
        return this.iTrailJpaRepository.findTrailsByEventIdAndCountry(eventId, country);
    }

    @Override
    public void save(Trail trail) {
        try {
            String wktCoordinates = trail.convertBytesToString();
            this.iTrailJpaRepository.save(trail.getTrailId(), trail.getCountry(), trail.getTrailname(), trail.getMaintainer(), wktCoordinates);
        } catch (ParseException e) {
            logger.error("Error while parsing wkt String for trail {}", trail.getTrailId());
        }
    }

    @Override
    public int delete(String trailId, List<String> publishers) {
        return this.iTrailJpaRepository.delete(trailId, publishers);
    }
}

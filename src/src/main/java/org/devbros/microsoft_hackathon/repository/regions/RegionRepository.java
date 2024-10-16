package org.devbros.microsoft_hackathon.repository.regions;

import lombok.Getter;
import org.devbros.microsoft_hackathon.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
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
public class RegionRepository implements IRegionRepository {
    private static final Logger logger = LoggerFactory.getLogger(RegionRepository.class.getName());

    private final IRegionJpaRepository iRegionJpaRepository;
    private final ExecutorService executorService;

    @Autowired
    public RegionRepository(IRegionJpaRepository iRegionJpaRepository){
        this.iRegionJpaRepository = iRegionJpaRepository;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    @Override
    public List<Region> findUniqueRegionName(String regionName, String country){
        Phaser phaser = new Phaser(1);
        logger.info("Try to identify region by name: " + regionName);
        long offset = 0;  // start from page 0

        List<Region> slice = this.iRegionJpaRepository.findRegionsByCountry(country, offset);
        TopMatchingHolder topMatchingHolder = new TopMatchingHolder();

        do {
            logger.info("Next slice - size: " + slice.size());
            logger.info(String.valueOf(offset));

            NameMatcher<Region> nameMatcher = new NameMatcher<>();
            // Process each entity
            phaser.register();
            List<Region> finalSlice = slice;
            this.executorService.submit(new Worker(phaser, () -> {
                finalSlice.forEach(region -> nameMatcher.match(regionName, region));

                Region topMatching = nameMatcher.getT();
                if (topMatching != null){
                    logger.info("Identified top matching.");
                    topMatchingHolder.setTopMatching(topMatching, nameMatcher.getMatchingScore());
                }
            }));

            // Update pageable to the next page
            offset = slice.get((slice.size() - 1)).getId();
            slice = this.iRegionJpaRepository.findRegionsByCountry(country, offset);
        } while (slice != null && !slice.isEmpty());

        phaser.arriveAndAwaitAdvance();
        return topMatchingHolder.topMatching != null ? List.of(topMatchingHolder.topMatching) : new ArrayList<>();
    }

    @Override
    public List<Region> findRegionByRegionNameAndCountry(String regionName, String country) {
        List<Region> regions = findUniqueRegionName(regionName, country);

        if (!regions.isEmpty()){
            logger.info("Identified top matching region");
            return this.iRegionJpaRepository.findAllByCountryAndName(country, regions.get(0).getName());
        }
        return regions;
    }

    @Getter
    static class TopMatchingHolder {
        private final Object lock = new Object();
        private Region topMatching;
        private double matchingScore;

        public void setTopMatching(Region topMatching, double matchingScore) {
            synchronized (lock){
                if (matchingScore > this.matchingScore){
                    this.topMatching = topMatching;
                }
            }
        }
    }
}

package org.devbros.microsoft_hackathon.repository.regions;

import org.devbros.microsoft_hackathon.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RegionRepository implements IRegionRepository {
    private static final Logger logger = LoggerFactory.getLogger(RegionRepository.class.getName());

    private final IRegionJpaRepository iRegionJpaRepository;
    private final NameMatcher<Region> nameMatcher;

    @Autowired
    public RegionRepository(IRegionJpaRepository iRegionJpaRepository, NameMatcher<Region> nameMatcher){
        this.iRegionJpaRepository = iRegionJpaRepository;
        this.nameMatcher = nameMatcher;
    }

    @Override
    public List<Region> findRegionByRegionNameAndCountry(String regionName, String country) {
        logger.info("Try to identify region by name: " + regionName);
        long offset = 0;  // start from page 0

        List<Region> slice = this.iRegionJpaRepository.findRegionsByCountry(country, offset);

        do {
            logger.info("Next slice - size: " + slice.size());
            logger.info(String.valueOf(offset));

            slice.forEach(region -> {
                // Process each entity
                this.nameMatcher.match(regionName, region);
            });
            // Update pageable to the next page
            offset = slice.get((slice.size() - 1)).getId();
            slice = this.iRegionJpaRepository.findRegionsByCountry(country, offset);
        } while (slice != null && !slice.isEmpty());

        Region topMatching = this.nameMatcher.getTopMatchingEntity();

        if (topMatching != null){
            logger.info("Identified top matching region");
            return this.iRegionJpaRepository.findAllByCountryAndName(country, topMatching.getName());
        }
        return new ArrayList<>();
    }
}

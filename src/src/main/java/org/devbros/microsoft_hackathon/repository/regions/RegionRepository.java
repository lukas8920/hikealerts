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
        int pageNumber = 0;  // start from page 0
        int pageSize = 200;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Slice<Region> slice;

        do {
            slice = this.iRegionJpaRepository.findRegionsByCountry(country, pageable);
            slice.getContent().forEach(region -> {
                // Process each entity
                this.nameMatcher.match(regionName, region);
            });
            pageable = pageable.next();  // Move to the next page
        } while (slice.hasContent());

        Region topMatching = this.nameMatcher.getTopMatchingEntity();

        if (topMatching != null){
            logger.info("Identified top matching region");
            return this.iRegionJpaRepository.findAllByCountryAndName(country, topMatching.getName());
        }
        return new ArrayList<>();
    }
}

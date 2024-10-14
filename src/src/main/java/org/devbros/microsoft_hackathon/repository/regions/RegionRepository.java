package org.devbros.microsoft_hackathon.repository.regions;

import org.devbros.microsoft_hackathon.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RegionRepository implements IRegionRepository {
    private final IRegionJpaRepository iRegionJpaRepository;
    private final NameMatcher<Region> nameMatcher;

    @Autowired
    public RegionRepository(IRegionJpaRepository iRegionJpaRepository, NameMatcher<Region> nameMatcher){
        this.iRegionJpaRepository = iRegionJpaRepository;
        this.nameMatcher = nameMatcher;
    }

    @Override
    public List<Region> findRegionByRegionNameAndCountry(String regionName, String country) {
        int pageNumber = 0;  // start from page 0
        int pageSize = 200;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Slice<Region> slice;

        do {
            slice = this.iRegionJpaRepository.findRegionsByCountry(country, pageable);
            slice.getContent().forEach(region -> {
                // Process each entity
                this.nameMatcher.match(country, region);
            });
            pageable = pageable.next();  // Move to the next page
        } while (slice.hasContent());

        Region topMatching = this.nameMatcher.getTopMatchingEntity();

        return topMatching == null ? new ArrayList<>() : this.iRegionJpaRepository.findAllByCountryAndName(country, topMatching.getName());
    }
}

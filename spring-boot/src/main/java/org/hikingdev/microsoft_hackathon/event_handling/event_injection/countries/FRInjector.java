package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

public class FRInjector extends RegionInjector {
    public FRInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, ITrailRepository iTrailRepository, IRegionRepository iRegionRepository) {
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }

    @Override
    protected List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region) {
        return this.iTrailRepository.findTrailsByNameCodeAndCountry(polygon, region.getCountry(), region.getCode());
    }

    @Override
    protected List<Region> findRegionsInDatabase(String regionName, String country) {
        return this.iRegionRepository.findUniqueRegionName(regionName, country);
    }
}

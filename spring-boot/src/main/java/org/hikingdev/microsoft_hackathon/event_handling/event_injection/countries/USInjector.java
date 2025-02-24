package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Region;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

public class USInjector extends RegionInjector {
    private static final String URL = "https://www.nps.gov/";

    public USInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, ITrailRepository iTrailRepository, IRegionRepository iRegionRepository) {
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }

    @Override
    protected void overwriteUrl(Event event) {
        if (event.getPublisherId() == 1L && (event.getUrl() == null || event.getUrl().isEmpty())){
            event.setUrl(URL);
        } else {
            event.setUrl(event.getUrl());
        }
    }

    @Override
    protected List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region) {
        return this.iTrailRepository.findTrailsByNameCodeAndCountry(polygon, region.getCountry(), region.getCode());
    }

    @Override
    protected List<Region> findRegionsInDatabase(String regionName, String country) {
        return this.iRegionRepository.findUniqueRegionName(regionName, country);
    }

    @Override
    protected void saveEvent(Event e){
        this.iEventRepository.save(e, true);
    }
}

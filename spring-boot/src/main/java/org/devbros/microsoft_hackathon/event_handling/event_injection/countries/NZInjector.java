package org.devbros.microsoft_hackathon.event_handling.event_injection.countries;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.devbros.microsoft_hackathon.event_handling.event_injection.matcher.NameMatcher;
import org.devbros.microsoft_hackathon.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

import java.util.ArrayList;
import java.util.List;

public class NZInjector extends BaseCountryInjector {
    private static final String URL = "https://www.doc.govt.nz/";

    public NZInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, ITrailRepository iTrailRepository, IRegionRepository iRegionRepository) {
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }

    @Override
    protected List<Event> identifyTrail(RawEvent rawEvent, Event event, OpenAiEvent openAiEvent) throws ParseException {
        List<Event> events = new ArrayList<>();
        //find best matching trail
        Trail trail = this.iTrailRepository.searchTrailByNameAndCountry(openAiEvent.getTrailName(), event.getCountry(), super.provideNameMatcher());
        if (trail != null){
            event.setTrailIds(List.of(trail.getId()));
            event.calculateMidCoordinate(trail);
            events.add(event);
        }
        return events;
    }

    @Override
    protected void overwriteUrl(Event event) {
        event.setUrl(URL);
    }

    @Override
    protected List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region) {
        return this.iTrailRepository.findTrailsInRegion(polygon, region.getCountry());
    }

    @Override
    protected List<Region> findRegionsInDatabase(String regionName, String country) {
        return this.iRegionRepository.findUniqueRegionName(regionName, country);
    }
}

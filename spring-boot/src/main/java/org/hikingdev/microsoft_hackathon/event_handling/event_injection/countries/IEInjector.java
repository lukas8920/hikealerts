package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;

import java.util.ArrayList;
import java.util.List;

public class IEInjector extends BaseCountryInjector {
    public IEInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, ITrailRepository iTrailRepository, IRegionRepository iRegionRepository) {
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }

    @Override
    protected void overwriteUrl(Event event) {
        event.setUrl(event.getUrl());
    }

    @Override
    protected List<Event> mapTrailsToEvents(OpenAiEvent openAiEvent, RawEvent rawEvent, Event event) throws ParseException {
        List<Event> events = new ArrayList<>();
        String[] extractedInfos = rawEvent.getDescription().split("\\.", 2);

        if (extractedInfos.length >= 2 && extractedInfos[0] != null){
            String description = extractedInfos[1].trim();
            String trail = extractedInfos[0];

            event.setDescription(description);

            events.addAll(identifyTrail(event, trail));
        }
        return events;
    }

    protected List<Event> identifyTrail(Event event, String trailname) throws ParseException {
        List<Event> events = new ArrayList<>();
        //find best matching trail
        Trail trail = this.iTrailRepository.searchTrailByNameAndCountry(trailname, event.getCountry(), provideNameMatcher());
        if (trail != null){
            event.setTrailIds(List.of(trail.getId()));
            event.calculateMidCoordinate(trail);
            events.add(event);
        }
        return events;
    };

    @Override
    protected List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region) {
        // not imeplemented for Ireland
        return null;
    }

    @Override
    protected List<Region> findRegionsInDatabase(String regionName, String country) {
        // not implemented for Ireland
        return null;
    }
}
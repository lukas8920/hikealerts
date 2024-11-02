package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GenericPenalizeDict;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.NameMatcher;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.country.NZWeightDict;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
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
    protected NameMatcher<Trail> provideNameMatcher(){
        return new NameMatcher<>(NZWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, 0.15, 0.62);
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

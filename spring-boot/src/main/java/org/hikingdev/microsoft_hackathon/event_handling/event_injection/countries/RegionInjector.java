package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RegionInjector extends BaseInjector {
    private static final Logger logger = LoggerFactory.getLogger(RegionInjector.class.getName());

    public RegionInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository,
                          ITrailRepository iTrailRepository, IRegionRepository iRegionRepository){
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }

    protected List<Event> mapTrailsToEvents(OpenAiEvent openAiEvent, RawEvent rawEvent, Event event) throws ParseException {
        List<Event> events = new ArrayList<>();
        if (openAiEvent
                .getTrailName() != null && openAiEvent.getTrailName().length() > 3){
            events.addAll(identifyTrail(rawEvent, event, openAiEvent));
        } else {
            events.addAll(identifyTrailsViaRegion(event));
        }
        return events;
    }

    protected List<Event> identifyTrail(RawEvent rawEvent, Event event, OpenAiEvent openAiEvent) throws ParseException {
        List<Event> events = new ArrayList<>();
        if (rawEvent.getUnitCode() != null){
            //find best matching trail
            Trail trail = this.iTrailRepository.searchTrailByNameUnitCodeAndCountry(openAiEvent.getTrailName(), rawEvent.getUnitCode(), event.getCountry(), provideNameMatcher());
            if (trail != null){
                event.setTrailIds(List.of(trail.getId()));
                event.calculateMidCoordinate(trail);
                events.add(event);
            }
        }
        return events;
    };

    protected List<Event> identifyTrailsViaRegion(Event event) throws ParseException {
        List<Event> events = new ArrayList<>();
        List<Region> regions = findRegionsInDatabase(event.getRegion(), event.getCountry());
        if (!regions.isEmpty()){
            logger.info("Number of regions: " + regions.size());
            for (Region region: regions){
                logger.info("Next region: " + region.getName());
                WKBReader wkbReader = new WKBReader();
                Polygon polygon = (Polygon) wkbReader.read(region.getPolygon());
                List<Trail> trails = this.findTrailsInDatabaseWithRegion(polygon, region);
                if (!trails.isEmpty()){
                    Event tmpEvent = new Event(event);
                    if (trails.size() > 1) {
                        tmpEvent.calculateMidCoordinate(trails.stream().map(Trail::getCoordinates).collect(Collectors.toList()));
                    } else {
                        tmpEvent.calculateMidCoordinate(trails.get(0));
                    }
                    tmpEvent.setTrailIds(trails.stream().map(Trail::getId).collect(Collectors.toList()));
                    events.add(tmpEvent);
                }
            }
        }
        return events;
    }

    // Implemented are identification via code or search via country solely / which takes significant longer
    protected abstract List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region);

    protected abstract List<Region> findRegionsInDatabase(String regionName, String country);
}

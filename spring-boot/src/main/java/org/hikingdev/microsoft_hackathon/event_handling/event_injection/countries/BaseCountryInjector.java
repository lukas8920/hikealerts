package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GenericPenalizeDict;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GenericWeightDict;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.NameMatcher;
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

public abstract class BaseCountryInjector {
    private static final Logger logger = LoggerFactory.getLogger(BaseCountryInjector.class.getName());

    private static final double GENERIC_MATCHER_THRESHOLD = 0.175;
    private static final double GENERIC_LEVENSHTEIN_WEIGHT = 0.62;

    protected final IRawEventRepository iRawEventRepository;
    protected final IEventRepository iEventRepository;
    protected final ITrailRepository iTrailRepository;
    protected final IRegionRepository iRegionRepository;

    public BaseCountryInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository,
                               ITrailRepository iTrailRepository, IRegionRepository iRegionRepository){
        this.iRawEventRepository = iRawEventRepository;
        this.iEventRepository = iEventRepository;
        this.iTrailRepository = iTrailRepository;
        this.iRegionRepository = iRegionRepository;
    }

    public boolean matchTrails(OpenAiEvent openAiEvent) throws ParseException {
        logger.info("Identify raw event with {}, {}", openAiEvent.getEventId(), openAiEvent.getCountry());
        RawEvent rawEvent = this.iRawEventRepository.findRawEvent(openAiEvent.getEventId(), openAiEvent.getCountry());
        if (rawEvent == null){
            return false;
        }

        Event event = new Event(rawEvent, openAiEvent);
        event.parseTimeInterval(openAiEvent.getFromDate(), openAiEvent.getToDate());

        List<Event> events =  mapTrailsToEvents(openAiEvent, rawEvent, event);

        if (events.isEmpty()){
            logger.info("Could not identify event for {} in country {}", openAiEvent.getEventId(), openAiEvent.getCountry());
            return false;
        }

        events.forEach(e -> {
            overwriteUrl(e);
            this.iEventRepository.save(e);
        });
        logger.info("Saved event to db.");
        return true;
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

    protected NameMatcher<Trail> provideNameMatcher(){
        return new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, GENERIC_MATCHER_THRESHOLD, GENERIC_LEVENSHTEIN_WEIGHT);
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

    protected abstract void overwriteUrl(Event event);

    // Implemented are identification via code or search via country solely / which takes significant longer
    protected abstract List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region);

    protected abstract List<Region> findRegionsInDatabase(String regionName, String country);
}

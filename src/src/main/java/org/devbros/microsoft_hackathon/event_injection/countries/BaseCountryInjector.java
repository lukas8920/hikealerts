package org.devbros.microsoft_hackathon.event_injection.countries;

import org.devbros.microsoft_hackathon.event_injection.entities.*;
import org.devbros.microsoft_hackathon.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseCountryInjector {
    private static final Logger logger = LoggerFactory.getLogger(BaseCountryInjector.class.getName());

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
        RawEvent rawEvent = this.iRawEventRepository.findRawEvent(openAiEvent.getEventId(), openAiEvent.getCountry());
        if (rawEvent == null){
            return false;
        }

        Event event = new Event(rawEvent, openAiEvent);
        event.parseTimeInterval(openAiEvent.getFromDate(), openAiEvent.getToDate());

        List<Event> events =  mapTrailsToEvents(openAiEvent, rawEvent, event);

        if (events.isEmpty()){
            return false;
        }
        this.iEventRepository.save(events);
        return true;
    }

    private List<Event> mapTrailsToEvents(OpenAiEvent openAiEvent, RawEvent rawEvent, Event event) throws ParseException {
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
            Trail trail = this.iTrailRepository.searchTrailByNameUnitCodeAndCountry(openAiEvent.getTrailName(), rawEvent.getUnitCode(), event.getCountry());
            if (trail != null){
                event.setTrailId(trail.getId());
                event.setDisplayMidCoordinate(true);
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
                for (Trail trail: trails){
                    Event tmpEvent = new Event(event);
                    tmpEvent.setTrailId(trail.getId());
                    tmpEvent.calculateMidCoordinate(trail);
                    // then we assume that trails belong together
                    tmpEvent.setHelperTrailName(trail.getTrailname() + " / " + trail.getMaplabel());
                    logger.info(tmpEvent.getHelperTrailName());
                    tmpEvent.setDisplayMidCoordinate(false);
                    events.add(tmpEvent);
                }
            }
        }
        //set Display Mid Coordinate flag for one part of the trail
        if (events.size() > 0){
            Map<String, Event> middleEvents = selectMiddleTrails(events);
            middleEvents.values().forEach(tmpEvent -> tmpEvent.setDisplayMidCoordinate(true));
        }
        return events;
    }

    private Map<String, Event> selectMiddleTrails(List<Event> events){
        Map<String, List<Event>> groupedByString = events.stream()
                .collect(Collectors.groupingBy(Event::getHelperTrailName));

        // For each group, get the middle object
        return groupedByString.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,  // The string key
                        entry -> {
                            List<Event> objects = entry.getValue();
                            int middleIndex = objects.size() / 2;  // Get middle index (rounds down for even size)
                            return objects.get(middleIndex);       // Get the middle object
                        }
                ));
    }

    protected abstract List<Trail> findTrailsInDatabaseWithRegion(Polygon polygon, Region region);

    protected abstract List<Region> findRegionsInDatabase(String regionName, String country);
}

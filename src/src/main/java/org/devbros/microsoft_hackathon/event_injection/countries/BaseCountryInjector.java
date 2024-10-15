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

public abstract class BaseCountryInjector {
    private static final Logger logger = LoggerFactory.getLogger(BaseCountryInjector.class.getName());

    private final IRawEventRepository iRawEventRepository;
    private final IEventRepository iEventRepository;
    private final ITrailRepository iTrailRepository;
    private final IRegionRepository iRegionRepository;

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
            Trail trail = this.iTrailRepository.findTrailByNameUnitCodeAndCountry(openAiEvent.getTrailName(), rawEvent.getUnitCode(), event.getCountry());
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
        List<Region> regions = this.iRegionRepository.findRegionByRegionNameAndCountry(event.getRegion(), event.getCountry());
        if (!regions.isEmpty()){
            logger.info("Number of regions: " + regions.size());
            for (Region region: regions){
                logger.info("Next region: " + region.getName());
                WKBReader wkbReader = new WKBReader();
                Polygon polygon = (Polygon) wkbReader.read(region.getPolygon());
                // todo: in case of us search by unit code instead of just country
                List<Trail> trails = this.iTrailRepository.findTrailsInRegion(polygon, event.getCountry());
                for (Trail trail: trails){
                    Event tmpEvent = new Event(event);
                    tmpEvent.setTrailId(trail.getId());
                    event.calculateMidCoordinate(trail);
                    event.setDisplayMidCoordinate(false);
                    events.add(event);
                }
            }
        }
        //set Display Mid Coordinate flag for one part of the trail
        if (events.size() > 0){
            int index = (int) Math.ceil(events.size() / 2);
            events.get(index).setDisplayMidCoordinate(true);
        }
        return events;
    }
}

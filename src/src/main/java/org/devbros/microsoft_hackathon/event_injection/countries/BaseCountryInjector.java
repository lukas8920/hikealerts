package org.devbros.microsoft_hackathon.event_injection.countries;

import org.devbros.microsoft_hackathon.event_injection.entities.*;
import org.devbros.microsoft_hackathon.event_injection.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.trails.ITrailRepository;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCountryInjector {
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

    public boolean matchTrails(OpenAiEvent openAiEvent){
        RawEvent rawEvent = this.iRawEventRepository.findRawEvent(openAiEvent.getEventId(), openAiEvent.getCountry());
        if (rawEvent == null){
            return false;
        }

        Event event = new Event(rawEvent, openAiEvent);
        event.parseTimeInterval(openAiEvent.getFromDate(), openAiEvent.getToDate());

        List<Event> events = mapTrailsToEvents(openAiEvent, rawEvent, event);

        if (events.isEmpty()){
            return false;
        }
        this.iEventRepository.save(events);
        return true;
    }

    private List<Event> mapTrailsToEvents(OpenAiEvent openAiEvent, RawEvent rawEvent, Event event) {
        List<Event> events = new ArrayList<>();
        if (openAiEvent
                .getTrailName() != null && openAiEvent.getTrailName().length() > 3){
            events.addAll(identifyTrail(rawEvent, event));
        } else {
            events.addAll(identifyTrailsViaRegion(event));
        }
        return events;
    }

    protected List<Event> identifyTrail(RawEvent rawEvent, Event event){
        List<Event> events = new ArrayList<>();
        if (rawEvent.getUnitCode() != null){
            //find best matching trail
            //todo: algorithm implementation / paging through datasets
            Trail trail = this.iTrailRepository.findTrailByUnitCodeAndCountry(rawEvent.getUnitCode(), event.getCountry());
            if (trail != null){
                event.setTrailId(trail.getId());
                event.setDisplayMidCoordinate(true);
                event.calculateMidCoordinate(trail);
                events.add(event);
            }
        }
        return events;
    };

    protected List<Event> identifyTrailsViaRegion(Event event){
        List<Event> events = new ArrayList<>();
        //todo: algorithm implementation / paging through datasets
        Region region = this.iRegionRepository.findRegionByRegionName(event.getRegion());
        if (region != null){
            //todo: implement finding point in polygon with sql syntax?
            List<Trail> trails = this.iTrailRepository.findTrailsInRegion(region.getPolygon());
            trails.forEach(trail -> {
                Event tmpEvent = new Event(event);
                tmpEvent.setTrailId(trail.getId());
                event.calculateMidCoordinate(trail);
                event.setDisplayMidCoordinate(false);
                events.add(event);
            });
        }
        //set Display Mid Coordinate flag for one part of the trail
        if (events.size() > 0){
            int index = (int) Math.ceil(events.size() / 2);
            events.get(index).setDisplayMidCoordinate(true);
        }
        return events;
    }
}

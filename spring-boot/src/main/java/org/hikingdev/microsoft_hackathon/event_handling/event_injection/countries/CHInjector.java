package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.io.ParseException;

import java.util.ArrayList;
import java.util.List;

public class CHInjector extends BaseInjector {
    public CHInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, ITrailRepository iTrailRepository, IRegionRepository iRegionRepository) {
        super(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
    }

    @Override
    protected List<Event> mapTrailsToEvents(OpenAiEvent openAiEvent, RawEvent rawEvent, Event event) throws ParseException {
        List<Event> events = new ArrayList<>();

        event.setDescription(openAiEvent.getDescription());
        event.setTitle(openAiEvent.getTitle());

        events.addAll(identifyTrail(event));
        return events;
    }

    protected List<Event> identifyTrail(Event event) throws ParseException {
        List<Event> events = new ArrayList<>();
        // as in the case of Switzerland the trail id is matching the event_id
        Trail trail = this.iTrailRepository.findTrailByIdAndCountry(event.getEvent_id(), event.getCountry());
        if (trail != null){
            event.setTrailIds(List.of(trail.getId()));
            event.calculateMidCoordinate(trail);
            events.add(event);
        }
        return events;
    }

    @Override
    protected void overwriteUrl(Event event) {
        event.setUrl(event.getUrl());
    }
}

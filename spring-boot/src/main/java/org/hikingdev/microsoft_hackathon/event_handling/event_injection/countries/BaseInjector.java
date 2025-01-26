package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GenericPenalizeDict;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GenericWeightDict;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.NameMatcher;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class BaseInjector {
    private static final Logger logger = LoggerFactory.getLogger(BaseInjector.class);

    protected final IRawEventRepository iRawEventRepository;
    protected final IEventRepository iEventRepository;
    protected final ITrailRepository iTrailRepository;
    protected final IRegionRepository iRegionRepository;

    private static final double GENERIC_MATCHER_THRESHOLD = 0.175;
    private static final double GENERIC_LEVENSHTEIN_WEIGHT = 0.62;

    public BaseInjector(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository,
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
            this.saveEvent(event);
        });
        logger.info("Saved event to db.");
        return true;
    }

    protected void saveEvent(Event e){
        this.iEventRepository.save(e, false);
    }

    protected NameMatcher<Trail> provideNameMatcher(){
        return new NameMatcher<>(GenericWeightDict.lowerWeightDict, GenericPenalizeDict.penalizeDict, GENERIC_MATCHER_THRESHOLD, GENERIC_LEVENSHTEIN_WEIGHT);
    }

    protected abstract List<Event> mapTrailsToEvents(OpenAiEvent openAiEvent, RawEvent rawEvent, Event event) throws ParseException;

    // countries which provide no url may overwrite this method
    protected void overwriteUrl(Event event){
        event.setUrl(event.getUrl());
    };
}

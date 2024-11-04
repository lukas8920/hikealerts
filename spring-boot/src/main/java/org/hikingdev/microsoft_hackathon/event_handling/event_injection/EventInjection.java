package org.hikingdev.microsoft_hackathon.event_handling.event_injection;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries.BaseCountryInjector;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries.NZInjector;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries.USInjector;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.map_layer.MapLayerService;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.io.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventInjection implements IEventInjection {
    private static final Logger logger = LoggerFactory.getLogger(EventInjection.class);

    private final IRawEventRepository iRawEventRepository;
    private final IEventRepository iEventRepository;
    private final ITrailRepository iTrailRepository;
    private final IRegionRepository iRegionRepository;

    private final MapLayerService mapLayerService;

    @Autowired
    public EventInjection(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository,
                          ITrailRepository iTrailRepository, IRegionRepository iRegionRepository, MapLayerService mapLayerService){
        this.iRawEventRepository = iRawEventRepository;
        this.iEventRepository = iEventRepository;
        this.iTrailRepository = iTrailRepository;
        this.iRegionRepository = iRegionRepository;

        this.mapLayerService = mapLayerService;
    }

    @Override
    public List<Message> injectEvent(List<OpenAiEvent> openAiEvents) {
        List<Message> errorMessages = new ArrayList<>();

        // filter events with valid input
        List<OpenAiEvent> processableEvents = validateOpenAiInputs(openAiEvents, errorMessages);

        /*
           Delete for each openaievent
           Needs to be done before event injection!
         */
        processableEvents.forEach(this.iEventRepository::deleteByOpenAiEvent);

        // trigger event injection
        processableEvents.forEach(openAiEvent -> {
            logger.info("Try to inject: " + openAiEvent.getEventId());
            BaseCountryInjector injector = openAiEvent.getCountry() != null ? assignCountryInjector(openAiEvent) : null;
            if (injector == null){
                Message message = new Message(openAiEvent.getEventId(), "Invalid country: " + openAiEvent.getCountry());
                logger.error("No valid injector for event " + openAiEvent.getEventId());
                errorMessages.add(message);
                return;
            }

            boolean flag = false;
            try {
                flag = injector.matchTrails(openAiEvent);
            } catch (ParseException e) {
                logger.error("Not able to create event because mid point calculation failed.", e);
            } catch (Exception e){
                logger.error("Other fatal error, " + e);
            }

            if (!flag){
                Message message = new Message(openAiEvent.getEventId(), "Processing the event was not possible because there is no matching source data.");
                errorMessages.add(message);
            }
        });

        // request geojson layer update if there have been events added to the db
        if (errorMessages.size() != openAiEvents.size()){
            logger.info("Updating geojson file due to event changes ...");
            this.mapLayerService.requestGeoJsonFileUpdate();
        } else {
            logger.info("There are not updates for the map layer, hence geojson file is not refreshed.");
        }

        if (!errorMessages.isEmpty()){
            return errorMessages;
        }

        return List.of(new Message("0", "All events processed."));
    }

    private List<OpenAiEvent> validateOpenAiInputs(List<OpenAiEvent> openAiEvents, List<Message> errorMessages) {
        List<OpenAiEvent> processableEvents = new ArrayList<>();
        // check valid input parameters before injecting events
        openAiEvents.forEach(openAiEvent -> {
            if (openAiEvent.getCountry() == null || openAiEvent.getCountry().length() != 2){
                Message message = new Message(openAiEvent.getEventId(), "Invalid country: " + openAiEvent.getCountry());
                logger.error("Invalid country for " + openAiEvent.getEventId());
                errorMessages.add(message);
                return;
            }
            processableEvents.add(openAiEvent);
        });
        logger.info("Validated all openai events.");
        return processableEvents;
    }

    protected BaseCountryInjector assignCountryInjector(OpenAiEvent openAiEvent) {
        switch (openAiEvent.getCountry()){
            case "US":
                return new USInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            case "NZ":
                return new NZInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            default:
                return null;
        }
    }
}

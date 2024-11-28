package org.hikingdev.microsoft_hackathon.event_handling.event_injection;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries.*;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.PbfTile;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.map_layer.MapLayerService;
import org.hikingdev.microsoft_hackathon.map_layer.Tile;
import org.hikingdev.microsoft_hackathon.map_layer.TileGenerator;
import org.hikingdev.microsoft_hackathon.map_layer.TileVectorService;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.util.TileUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventInjection implements IEventInjection {
    private static final Logger logger = LoggerFactory.getLogger(EventInjection.class);

    protected final IRawEventRepository iRawEventRepository;
    protected final IEventRepository iEventRepository;
    protected final ITrailRepository iTrailRepository;
    protected final IRegionRepository iRegionRepository;
    private final MapLayerService mapLayerService;
    private final TileVectorService tileVectorService;

    @Autowired
    public EventInjection(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository, TileVectorService tileVectorService,
                          ITrailRepository iTrailRepository, IRegionRepository iRegionRepository, MapLayerService mapLayerService){
        this.tileVectorService = tileVectorService;
        this.iRawEventRepository = iRawEventRepository;
        this.iEventRepository = iEventRepository;
        this.iTrailRepository = iTrailRepository;
        this.iRegionRepository = iRegionRepository;
        this.mapLayerService = mapLayerService;
    }

    public List<Message> injectEvent(List<OpenAiEvent> openAiEvents){
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
            BaseInjector injector = openAiEvent.getCountry() != null ? assignCountryInjector(openAiEvent) : null;
            if (injector == null){
                Message message = new Message(openAiEvent.getEventId(), openAiEvent.getCountry(), "Invalid country: " + openAiEvent.getCountry());
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
                Message message = new Message(openAiEvent.getEventId(), openAiEvent.getCountry(), "Processing the event was not possible because there is no matching source data.");
                errorMessages.add(message);
            }
        });

        // request geojson layer update if there have been events added to the db
        if (errorMessages.size() != openAiEvents.size()){
            logger.info("Updating geojson file due to event changes ...");
            this.mapLayerService.requestGeoJsonFileUpdate();

            List<byte[]> trails = this.successfulTrails(openAiEvents, errorMessages);
            List<PbfTile> pbfTiles = this.inverseTileCoordinates(trails);
            this.updateCachedTiles(pbfTiles);
        } else {
            logger.info("There are not updates for the map layer, hence geojson file is not refreshed.");
        }

        if (!errorMessages.isEmpty()){
            return errorMessages;
        }

        return List.of(new Message("0", "All events processed."));
    }

    protected List<byte[]> successfulTrails(List<OpenAiEvent> openAiEvents, List<Message> messages){
        // remove matching id's and country's from openai list
        openAiEvents = openAiEvents.stream()
                .filter(o -> messages.stream().noneMatch(m -> m.getId().equals(o.getEventId())
                        && m.getCountry().equals(o.getCountry())))
                .collect(Collectors.toList());
        // get trail data by event_id and country
        List<Trail> trails = new ArrayList<>();
        openAiEvents.forEach(o -> trails.addAll(this.iTrailRepository.findTrailsByEventIdAndCountry(o.getEventId(), o.getCountry())));
        return trails.stream().map(Trail::getCoordinates).collect(Collectors.toList());
    }

    private void updateCachedTiles(List<PbfTile> pbfTiles){
        TileGenerator tileGenerator = this.tileVectorService.getTileGenerator();
        synchronized (TileVectorService.lock()){
            new Thread(() -> {
                logger.info("Request update for the tile cache.");
                pbfTiles.forEach(pbfTile ->
                        this.tileVectorService.generateTile(tileGenerator, pbfTile.getX(), pbfTile.getY(), pbfTile.getZ()));
                logger.info("Updated the tile cache with {} tiles.", pbfTiles.size());
            }).start();
        }
    }

    private List<PbfTile> inverseTileCoordinates(List<byte[]> trails){
        WKBReader wkbReader = new WKBReader();
        List<PbfTile> pbfTiles = new ArrayList<>();
        trails.forEach(trail -> {
            try {
                LineString line = (LineString) wkbReader.read(trail);
                Set<PbfTile> tmpPbfTiles = TileUtils.getIntersectedTiles(line, TileVectorService.MIN_ZOOM, TileVectorService.MAX_ZOOM);
                pbfTiles.addAll(tmpPbfTiles);
            } catch (ParseException e){
                logger.error("Error while parsing linestring during tile generation.");
            }
        });
        return pbfTiles;
    }

    private List<OpenAiEvent> validateOpenAiInputs(List<OpenAiEvent> openAiEvents, List<Message> errorMessages) {
        List<OpenAiEvent> processableEvents = new ArrayList<>();
        // check valid input parameters before injecting events
        openAiEvents.forEach(openAiEvent -> {
            if (openAiEvent.getCountry() == null || openAiEvent.getCountry().length() != 2){
                Message message = new Message(openAiEvent.getEventId(), openAiEvent.getCountry(), "Invalid country: " + openAiEvent.getCountry());
                logger.error("Invalid country for " + openAiEvent.getEventId());
                errorMessages.add(message);
                return;
            }
            processableEvents.add(openAiEvent);
        });
        logger.info("Validated all openai events.");
        return processableEvents;
    }

    protected BaseInjector assignCountryInjector(OpenAiEvent openAiEvent) {
        switch (openAiEvent.getCountry()){
            case "US":
                return new USInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            case "NZ":
                return new NZInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            case "IE":
                return new IEInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            case "CH":
                return new CHInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            default:
                return null;
        }
    }
}

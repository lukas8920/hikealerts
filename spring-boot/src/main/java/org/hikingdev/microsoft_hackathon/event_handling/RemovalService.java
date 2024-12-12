package org.hikingdev.microsoft_hackathon.event_handling;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.PbfTile;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.map_layer.TileGenerator;
import org.hikingdev.microsoft_hackathon.map_layer.TileVectorService;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.util.EventNotFoundException;
import org.hikingdev.microsoft_hackathon.util.InvalidationException;
import org.hikingdev.microsoft_hackathon.util.ScheduledService;
import org.hikingdev.microsoft_hackathon.util.TileUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RemovalService extends ScheduledService {
    private final Logger logger;

    private final IEventRepository iEventRepository;
    private final ITrailRepository iTrailRepository;
    private final TileVectorService tileVectorService;

    @Autowired
    public RemovalService(IEventRepository iEventRepository, @Qualifier("queueClient") QueueClient queueClient,
                          ITrailRepository iTrailRepository, TileVectorService tileVectorService) {
        super(queueClient);

        this.logger = LoggerFactory.getLogger(EventListenerService.class.getName());

        this.iEventRepository = iEventRepository;
        this.iTrailRepository = iTrailRepository;
        this.tileVectorService = tileVectorService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void processMessage(String messageBody) {
        RemovalEntity removalEntity;
        try {
            removalEntity = this.objectMapper.readValue(messageBody, RemovalEntity.class);
            getLogger().info("check event deletion from database for {} elemenst", removalEntity.getIds().size());
            Set<MapEvent> deletedEvents = this.iEventRepository.deleteEventsNotInList(removalEntity.getIds(), removalEntity.getCountry());
            refreshTiles(deletedEvents);
        } catch (JsonProcessingException e) {
            getLogger().error("Could not json parse: " + messageBody);
        }
    }

    private void refreshTiles(Set<MapEvent> deletedEvents){
        if (!deletedEvents.isEmpty()){
            WKBReader wkbReader = new WKBReader();
            TileGenerator tileGenerator = this.tileVectorService.getTileGenerator();

            logger.info("Refresh cached tiles.");
            deletedEvents.forEach(event -> {
                List<Trail> trails = this.iTrailRepository.findTrailsByEventIdAndCountry(event.getEvent_id(), event.getCountry());
                trails.forEach(trail -> {
                    try {
                        LineString lineString = (LineString) wkbReader.read(trail.getCoordinates());
                        Set<PbfTile> pbfTiles = TileUtils.getIntersectedTiles(lineString, TileVectorService.MIN_ZOOM, TileVectorService.MAX_ZOOM);
                        pbfTiles.forEach(pbfTile -> this.tileVectorService.generateTile(tileGenerator, pbfTile.getX(), pbfTile.getY(), pbfTile.getZ()));
                    } catch (ParseException e) {
                        logger.error("Error while parsing line string", e);
                    }
                });
            });
            logger.info("Refreshed all tiles.");
        }
    }

    public void removeEvent(String trail, String country, Long publisherId) throws EventNotFoundException, InvalidationException {
        // determine trail by country
        List<MapEvent> mapEvents = this.iEventRepository.findEventsByTrailAndCountry(trail, country);
        if (!mapEvents.isEmpty()){
            // filter on publisher
            mapEvents = mapEvents.stream()
                    .filter(mapEvent -> mapEvent.getPublisherId().equals(publisherId))
                    .collect(Collectors.toList());
            if (!mapEvents.isEmpty()){
                for(MapEvent mapEvent: mapEvents){
                    boolean flag = this.iEventRepository.deleteByIdAndPublisher(mapEvent.getId(), mapEvent.getPublisherId());
                    if (flag) {
                        refreshTiles(Set.of(mapEvent));
                    } else {
                        throw new EventNotFoundException("Event deletion for " + mapEvent.getId() + " not possible.");
                    }
                }
            } else {
                throw new InvalidationException("User is not authorized to delete events.");
            }
        } else {
            throw new EventNotFoundException("No events found for " + trail + ", " + country);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RemovalEntity {
        private String country;
        private List<String> ids;
    }
}

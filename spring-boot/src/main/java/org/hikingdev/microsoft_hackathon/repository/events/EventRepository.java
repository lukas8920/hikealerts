package org.hikingdev.microsoft_hackathon.repository.events;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hikingdev.microsoft_hackathon.event_handling.EventResponseMapper;
import org.hikingdev.microsoft_hackathon.event_handling.MapEventMapper;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventJpaRepository;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class EventRepository implements IEventRepository {
    private static final Logger logger = LoggerFactory.getLogger(EventRepository.class.getName());
    private static final String EVENTS_KEY = "events";

    private final IEventJpaRepository iEventJpaRepository;
    private final ITrailJpaRepository iTrailJpaRepository;
    private final IRawEventJpaRepository iRawEventJpaRepository;
    private final IPublisherRepository iPublisherRepository;
    private final RedisTemplate<String, MapEvent> redisTemplate;
    private final EntityManager entityManager;
    private final MapEventMapper mapEventMapper;
    private final EventResponseMapper eventResponseMapper;

    private static final Object lock = new Object();
    private static boolean isThreadRunning = false;

    @Autowired
    public EventRepository(IEventJpaRepository iEventJpaRepository, RedisTemplate<String, MapEvent> redisTemplate, MapEventMapper mapEventMapper,
                           EntityManager entityManager, IRawEventJpaRepository iRawEventJpaRepository, IPublisherRepository iPublisherRepository,
                           EventResponseMapper eventResponseMapper, ITrailJpaRepository iTrailJpaRepository){
        this.mapEventMapper = mapEventMapper;
        this.iPublisherRepository = iPublisherRepository;
        this.iEventJpaRepository = iEventJpaRepository;
        this.redisTemplate = redisTemplate;
        this.entityManager = entityManager;
        this.iRawEventJpaRepository = iRawEventJpaRepository;
        this.eventResponseMapper = eventResponseMapper;
        this.iTrailJpaRepository = iTrailJpaRepository;
    }

    @Transactional
    @Override
    public void save(Event event, boolean overrideData) {
        Publisher publisher = this.iPublisherRepository.findUserById(event.getPublisherId());

        if (publisher != null){
            try {
                // Convert ids to comma separated list
                String trailIds = event.getTrailIds().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                // Add to Redis
                logger.info("Add / update dataset to db and redis.");
                Event tmpEvent = this.iEventJpaRepository.saveEvent(event.getEvent_id(), event.getRegion(), event.getCountry(),
                        event.getCreateDatetime(), event.getFromDatetime(), event.getToDatetime(), event.getMidLongitudeCoordinate(),
                        event.getMidLatitudeCoordinate(), event.getTitle(), event.getDescription(),trailIds,
                        event.getPublisherId(), event.getUrl(), overrideData);
                tmpEvent.setTrailIds(event.getTrailIds());
                MapEvent mapEvent = this.mapEventMapper.map(tmpEvent, publisher);

                synchronized (lock){
                    if (isThreadRunning){
                        logger.info("Return as cache refreshing service updates db ...");
                        return;
                    }
                }

                if (overrideData){
                    logger.info("Delete existing map event before adding override event");
                    deleteExistingMapEvent(tmpEvent);
                }

                logger.info("Save mapEvent: " + mapEvent);
                redisTemplate.opsForZSet().add(EVENTS_KEY, mapEvent, tmpEvent.getId());
            } catch (Exception e){
                logger.error("Error while saving map event: ", e);
            }
        } else {
            logger.error("Error saving event {} as publisher {} does not exist.", event.getEvent_id(), event.getPublisherId());
        }
    }

    private void deleteExistingMapEvent(Event tmpEvent){
        Set<MapEvent> events = redisTemplate.opsForZSet().rangeByScore(EVENTS_KEY, tmpEvent.getId(), tmpEvent.getId());
        if (events != null) {
            for (Object event : events) {
                redisTemplate.opsForZSet().remove(EVENTS_KEY, event);
            }
        }
    }

    @Override
    public List<MapEvent> findEventsByTrailAndCountry(String trail, String country){
        List<Object[]> objects = this.iEventJpaRepository.findEventsByTrailAndCountry(trail, country);
        return objects.stream().map(mapEventMapper::map).collect(Collectors.toList());
    }

    @Override
    public List<MapEvent> refreshCache(){
        List<MapEvent> outputEvents = new ArrayList<>();
        logger.info("Start cache refreshing.");
        synchronized (lock){
            isThreadRunning = true;
        }
        try {
            redisTemplate.delete(EVENTS_KEY);
            logger.info("Deleted events from cache.");

            int offset = 0;
            List<MapEvent> mapEvents = findAllByOffsetAndLimit(offset, 100);
            logger.info("Retrieved first {} events from db.", mapEvents.size());
            while (!mapEvents.isEmpty()){
                outputEvents.addAll(mapEvents);
                for (MapEvent mapEvent : mapEvents) {
                    redisTemplate.opsForZSet().add(EVENTS_KEY, mapEvent, mapEvent.getId());
                }

                offset += 100;
                mapEvents = findAllByOffsetAndLimit(offset, 100);
                logger.debug("Retrieved next {} events.", mapEvents.size());
            }
        } catch (Exception e){
            logger.error("Error while persisting", e);
        } finally {
            logger.info("Finished cache refreshing");
            synchronized (lock){
                isThreadRunning = false;
                lock.notifyAll();
            }
        }
        return outputEvents;
    }

    @Override
    public List<MapEvent> findEvents(int offset, int limit) {
        // Get events from Redis
        synchronized (lock){
            while (isThreadRunning){
                try {
                    logger.info("Entering lock.");
                    lock.wait();
                    logger.info("Lock released.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Set<MapEvent> mapEvents = redisTemplate.opsForZSet().range(EVENTS_KEY, offset, offset + limit - 1);

        if (mapEvents == null || mapEvents.isEmpty()) {
            // If not present in cache, fetch from the database
            List<MapEvent> fetchedMapEvents = this.findAllByOffsetAndLimit(offset, limit);

            // Add events to Redis (using their IDs as scores)
            for (MapEvent mapEvent : fetchedMapEvents) {
                redisTemplate.opsForZSet().add(EVENTS_KEY, mapEvent, mapEvent.getId()); // Assuming event.getId() returns a unique score
            }

            logger.info("Fetched events from {} to {} via MS SQL.", offset, limit);
            return fetchedMapEvents;
        } else {
            logger.debug("Fetched events from {} to {} via Redis.", offset, limit);
        }

        return List.copyOf(mapEvents);
    }

    private List<MapEvent> findAllByOffsetAndLimit(int offset, int limit) {
        TypedQuery<Object[]> query = entityManager.createQuery(
                "SELECT e.id, e.title, e.description, p.name as publisher, p.status, e.createDatetime, e.midLatitudeCoordinate, e.midLongitudeCoordinate, e.event_id, e.country, e.publisherId, e.url, STRING_AGG(l, ','), p.copyright, p.license FROM Event e JOIN Publisher p ON p.id = e.publisherId JOIN e.trailIds l GROUP BY e.id, e.title, e.description, p.name, p.status, e.createDatetime, e.midLatitudeCoordinate, e.midLongitudeCoordinate, e.event_id, e.country, e.publisherId, e.url, p.copyright, p.license ORDER BY e.id", Object[].class);
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        List<Object[]> objects = query.getResultList();
        logger.debug("Fetched {} events with offset {}.", objects.size(), offset);

        return objects.stream().map(this.mapEventMapper::map).collect(Collectors.toList());
    }

    private List<MapEvent> findMapEventByIdAndCountry(String eventId, String country){
        List<Object[]> objects = this.iEventJpaRepository.findByEventIdAndCountry(eventId, country);
        return objects.stream().map(this.mapEventMapper::map).collect(Collectors.toList());
    }

    @Override
    @Transactional
    // raw event needs to stay in the database
    public void deleteByOpenAiEvent(OpenAiEvent openAiEvent){
        logger.info("Delete openai event " + openAiEvent.getEventId());
        List<MapEvent> mapEvent = findMapEventByIdAndCountry(openAiEvent.getEventId(), openAiEvent.getCountry());

        this.iEventJpaRepository.deleteByIdAndCountry(openAiEvent.getEventId(), openAiEvent.getCountry());

        // Remove keys that are not in the provided list
        if (!mapEvent.isEmpty()) {
            logger.info("Delete MapEvent: " + mapEvent);
            redisTemplate.opsForZSet().remove(EVENTS_KEY, mapEvent.toArray());
        }
    }

    @Override
    @Transactional
    public boolean deleteByIdAndPublisher(Long id, Long publisherId){
        logger.info("Delete by id " + id);
        List<Object[]> rawObjects = this.iEventJpaRepository.findByIdAndPublisher(id, publisherId);
        if (!rawObjects.isEmpty()){
            List<MapEvent> mapEvents = rawObjects.stream().map(this.mapEventMapper::map).toList();
            MapEvent mapEvent = mapEvents.get(0);
            logger.info("Delete mapEvent: " + mapEvent);

            this.iEventJpaRepository.deleteById(mapEvent.getId());
            this.iRawEventJpaRepository.deleteByIdAndCountry(mapEvent.getEvent_id(), mapEvent.getCountry());
            this.redisTemplate.opsForZSet().remove(EVENTS_KEY, mapEvent);
        } else {
            logger.info("No event with id {} found in database", id);
            return false;
        }
        logger.info("Deleted event with id {}", id);
        return true;
    }

    @Transactional
    public Set<MapEvent> deleteEventsNotInList(List<String> idsToKeep, String country) {
        // get list of ids from sql database
        List<Object[]> objEvents = this.iEventJpaRepository.findByEventIdsAndCountry(idsToKeep, country);
        List<MapEvent> events = objEvents.stream().map(this.mapEventMapper::map).toList();

        // Convert List to Set for faster lookup
        Set<Long> idsToKeepSet = events.stream().map(MapEvent::getId).collect(Collectors.toSet());
        Set<MapEvent> eventsToDelete = new HashSet<>(); // To collect keys to delete

        // Use RedisTemplate for scanning the sorted set
        synchronized (lock){
            while (isThreadRunning){
                try {
                    logger.info("Entering lock");
                    lock.wait();
                    logger.info("Lock released");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Cursor<ZSetOperations.TypedTuple<MapEvent>> cursor = redisTemplate.opsForZSet().scan(
                EVENTS_KEY,
                ScanOptions.scanOptions().match("*").count(1000).build()
        );

        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<MapEvent> tuple = cursor.next();
            MapEvent element = tuple.getValue(); // Get the member (ID as String)
            if (element != null){
                long id = element.getId(); // Parse the ID

                if (!idsToKeepSet.contains(id) && element.getCountry().equals(country) && element.getPublisherId() != 3L) {
                    eventsToDelete.add(element); // Collect IDs not in the list
                } else {
                    eventsToDelete.remove(element);
                }
            }
        }

        logger.info("Delete {} events in {}.", eventsToDelete.size(), country);
        // Remove keys that are not in the provided list
        if (!eventsToDelete.isEmpty()) {
            try {
                redisTemplate.opsForZSet().remove(EVENTS_KEY, eventsToDelete.toArray());
            } catch (Exception e){
                logger.error("Error during Redis Operation: " + e.getMessage());
                return new HashSet<>();
            }
        }

        // delete ids in raw events and events
        for (MapEvent event: eventsToDelete){
            try {
                logger.info("Delete {} / {}", event.getEvent_id(), event.getId());
                this.iEventJpaRepository.deleteByIdAndCountry(event.getEvent_id(), event.getCountry());
                this.iRawEventJpaRepository.deleteByIdAndCountry(event.getEvent_id(), event.getCountry());
                if (event.getCountry().equals("CH")){
                    this.iTrailJpaRepository.deleteAllByTrailIdAndCountry(event.getEvent_id(), event.getCountry());
                }
                return eventsToDelete;
            } catch (Exception e){
                logger.error("Error during deletion, ", e);
            }
        }
        return new HashSet<>();
    }

    public List<EventResponse> queryEvents(Double[] boundaries, String country, LocalDate fromDate, LocalDate toDate, LocalDate createDate, String createdBy, boolean nullDates, boolean returnGeometry, int limit, int offset){
        String selectQuery = buildQuery(boundaries, country, fromDate, toDate, createDate, createdBy, nullDates, returnGeometry);

        Query query = entityManager.createNativeQuery(selectQuery, Object[].class);

        if (country != null) {
            query.setParameter("country", country);
        }
        if (boundaries.length != 0) {
            query.setParameter("minx", boundaries[0]);
            query.setParameter("maxx", boundaries[2]);
            query.setParameter("miny", boundaries[1]);
            query.setParameter("maxy", boundaries[3]);
        }
        if (fromDate != null){
            query.setParameter("fromDate", fromDate.atTime(LocalTime.MIN));
        }
        if (toDate != null){
            query.setParameter("toDate", toDate.atTime(LocalTime.MAX));
        }
        if (createDate != null){
            query.setParameter("createDate", createDate.atTime(LocalTime.MIN));
        }

        query.setMaxResults(limit);
        query.setFirstResult(offset);

        List<Object[]> objects = query.getResultList();
        return objects.stream().map(this.eventResponseMapper::map).collect(Collectors.toList());
    }

    private String buildQuery(Double[] boundaries, String country, LocalDate fromDate, LocalDate toDate, LocalDate createDate, String createdBy, boolean nullDates, boolean returnGeometry){
        String boundaryClause = "e.mid_longitude_coordinate >= :minx AND e.mid_longitude_coordinate <= :maxx AND e.mid_latitude_coordinate >= :miny AND e.mid_latitude_coordinate <= :maxy";
        String countryClause = "e.country = :country";

        StringBuilder builder = new StringBuilder();
        builder.append("SELECT e.id, e.country, t.trailname, t.id, e.title, e.description, e.from_date_time, e.to_date_time, e.create_date_time, p.name, p.status, e.mid_longitude_coordinate, e.mid_latitude_coordinate, p.copyright, p.license");
        if (returnGeometry){
            builder.append(", t.coordinates.STAsBinary() as coordinates ");
        } else {
            builder.append(" ");
        }
        builder.append("FROM events e " +
                "INNER JOIN publisher p ON p.id = e.publisher_id " +
                "INNER JOIN events_trail_ids i ON i.event_id = e.id " +
                "INNER JOIN geodata_trails t ON t.id = i.trail_ids WHERE ");

        if (boundaries.length != 0 && country != null){
            builder.append(countryClause);
            builder.append(" AND ");
            builder.append(boundaryClause);
        } else if (boundaries.length != 0){
            builder.append(boundaryClause);
        } else {
            builder.append(countryClause);
        }

        if (createDate != null){
            builder.append(" AND ");
            builder.append("e.create_date_time >= :createDate");
        }

        if (createdBy != null){
            if (createdBy.equals("Official")){
                builder.append(" AND ");
                builder.append("p.status = 'OFFICIAL'");
            } else if (createdBy.equals("Community")){
                builder.append(" AND ");
                builder.append("p.status = 'COMMUNITY'");
            }
        }

        if (fromDate != null && !nullDates){
            builder.append(" AND ");
            builder.append("(e.from_date_time >= :fromDate AND e.from_date_time is not null)");
        } else if (fromDate != null){
            builder.append(" AND ");
            builder.append("(e.from_date_time >= :fromDate OR e.from_date_time is null)");
        }

        if (toDate != null && !nullDates){
            builder.append(" AND ");
            builder.append("(e.to_date_time <= :toDate AND e.to_date_time is not null)");
        } else if (toDate != null){
            builder.append(" AND ");
            builder.append("(e.to_date_time <= :toDate OR e.to_date_time is null)");
        }

        if (fromDate == null && toDate == null && !nullDates){
            builder.append(" AND e.from_date_time is not null and e.to_date_time is not null");
        }

        builder.append(" ORDER BY e.id");
        /*builder.append(" ORDER BY e.id OFFSET ");
        builder.append(offset);
        builder.append(" rows");*/

        return builder.toString();
    }
}

package org.devbros.microsoft_hackathon.repository.events;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.devbros.microsoft_hackathon.event_handling.MapEventMapper;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventJpaRepository;
import org.devbros.microsoft_hackathon.publisher_management.entities.Publisher;
import org.devbros.microsoft_hackathon.publisher_management.repository.IPublisherRepository;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EventRepository implements IEventRepository {
    private static final Logger logger = LoggerFactory.getLogger(EventRepository.class.getName());
    private static final String EVENTS_KEY = "events";

    private final IEventJpaRepository iEventJpaRepository;
    private final IRawEventJpaRepository iRawEventJpaRepository;
    private final IPublisherRepository iPublisherRepository;
    private final ITrailRepository iTrailRepository;
    private final RedisTemplate<String, MapEvent> redisTemplate;
    private final EntityManager entityManager;
    private final MapEventMapper mapEventMapper;

    @Autowired
    public EventRepository(IEventJpaRepository iEventJpaRepository, RedisTemplate<String, MapEvent> redisTemplate, MapEventMapper mapEventMapper,
                           EntityManager entityManager, IRawEventJpaRepository iRawEventJpaRepository, IPublisherRepository iPublisherRepository,
                           ITrailRepository iTrailRepository){
        this.mapEventMapper = mapEventMapper;
        this.iTrailRepository = iTrailRepository;
        this.iPublisherRepository = iPublisherRepository;
        this.iEventJpaRepository = iEventJpaRepository;
        this.redisTemplate = redisTemplate;
        this.entityManager = entityManager;
        this.iRawEventJpaRepository = iRawEventJpaRepository;
    }

    @Override
    public void save(Event event) {
        Publisher publisher = this.iPublisherRepository.findUserById(event.getPublisherId());
        Event oldEvent = this.iEventJpaRepository.findFirstByEventIdAndCountry(event.getEvent_id(), event.getCountry());

        if (publisher != null){
            try {
                if (oldEvent != null){
                    updateEvent(event, publisher, oldEvent);
                }
                MapEvent mapEvent = this.mapEventMapper.map(event, publisher);
                // Add to Redis
                logger.info("Add dataset to db and redis.");
                this.iEventJpaRepository.save(event);

                redisTemplate.opsForZSet().add(EVENTS_KEY, mapEvent, event.getId());
            } catch (Exception e){
                e.printStackTrace();
            }
        } else {
            logger.error("Error saving event {} as publisher {} does not exist.", event.getEvent_id(), event.getPublisherId());
        }
    }

    private void updateEvent(Event event, Publisher publisher, Event oldEvent) {
        List<Trail> oldTrails = this.iTrailRepository.findAllTrailsByIds(oldEvent.getTrailIds());
        List<Trail> newTrails = this.iTrailRepository.findAllTrailsByIds(event.getTrailIds());
        boolean trailIsNotPresent = oldTrails.stream().noneMatch(t -> t.getTrailname().equals(newTrails.get(0).getTrailname()));

        if (oldTrails.isEmpty() || trailIsNotPresent){
            logger.info("Add trail to existing trail ids of event");
            List<Long> trailIds = oldEvent.getTrailIds();
            trailIds.addAll(event.getTrailIds());
            event.setTrailIds(trailIds);
        } else {
            logger.info("Work with existing trail ids in db, as trail id is already present");
            event.setTrailIds(oldEvent.getTrailIds());
        }

        MapEvent oldMapEvent = this.mapEventMapper.map(oldEvent, publisher);
        redisTemplate.opsForZSet().remove(EVENTS_KEY, oldMapEvent);
        this.iEventJpaRepository.deleteById(oldEvent.getId());
    }

    @Override
    public List<MapEvent> findEvents(int offset, int limit) {
        // Get events from Redis
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
            logger.info("Fetched events from {} to {} via Redis.", offset, limit);
        }

        return List.copyOf(mapEvents);
    }

    private List<MapEvent> findAllByOffsetAndLimit(int offset, int limit) {
        TypedQuery<Object[]> query = entityManager.createQuery(
                "SELECT e.id, e.title, e.description, p.name as publisher, p.status, e.createDate, e.lat, e.lng, e.event_id, e.country, e.publisherId, e.url FROM MapEvent e JOIN Publisher p ON p.id = e.publisherId WHERE e.id >= :offset ORDER BY e.id", Object[].class);
        query.setParameter("offset", offset);
        query.setMaxResults(limit);

        return query.getResultList().stream().map(this.mapEventMapper::map).collect(Collectors.toList());
    }

    @Transactional
    public void deleteEventsNotInList(List<String> idsToKeep, String country) {
        // get list of ids from sql database
        String listedIds = idsToKeep.stream()
                .map(id -> "'" + id + "'")  // Convert each Long to String
                .collect(Collectors.joining(", ", "(", ")"));
        List<MapEvent> events = this.iEventJpaRepository.findByEventIdsAndCountry(listedIds, country);

        // Convert List to Set for faster lookup
        Set<Long> idsToKeepSet = events.stream().map(MapEvent::getId).collect(Collectors.toSet());
        Set<MapEvent> eventsToDelete = new HashSet<>(); // To collect keys to delete

        // Use RedisTemplate for scanning the sorted set
        Cursor<ZSetOperations.TypedTuple<MapEvent>> cursor = redisTemplate.opsForZSet().scan(
                EVENTS_KEY,
                ScanOptions.scanOptions().match("*").count(1000).build()
        );

        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<MapEvent> tuple = cursor.next();
            MapEvent element = tuple.getValue(); // Get the member (ID as String)
            if (element != null){
                long id = element.getId(); // Parse the ID

                if (!idsToKeepSet.contains(id)) {
                    eventsToDelete.add(element); // Collect IDs not in the list
                } else {
                    eventsToDelete.remove(element);
                }
            }
        }

        // Remove keys that are not in the provided list
        if (!eventsToDelete.isEmpty()) {
            redisTemplate.opsForZSet().remove(EVENTS_KEY, eventsToDelete.toArray());
        }

        // delete ids in raw events and events
        eventsToDelete.forEach(event -> {
            this.iEventJpaRepository.deleteById(event.getId());
            this.iRawEventJpaRepository.deleteByIdAndCountry(event.getEvent_id(), event.getCountry());
        });
    }
}

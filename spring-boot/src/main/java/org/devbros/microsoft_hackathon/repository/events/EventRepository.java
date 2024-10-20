package org.devbros.microsoft_hackathon.repository.events;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventJpaRepository;
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
    private static final String EVENTS_KEY = "events";

    private final IEventJpaRepository iEventJpaRepository;
    private final IRawEventJpaRepository iRawEventJpaRepository;
    private final RedisTemplate<String, Event> redisTemplate;
    private final EntityManager entityManager;

    @Autowired
    public EventRepository(IEventJpaRepository iEventJpaRepository, RedisTemplate<String, Event> redisTemplate,
                           EntityManager entityManager, IRawEventJpaRepository iRawEventJpaRepository){
        this.iEventJpaRepository = iEventJpaRepository;
        this.redisTemplate = redisTemplate;
        this.entityManager = entityManager;
        this.iRawEventJpaRepository = iRawEventJpaRepository;
    }

    @Override
    public void save(Event event) {
        this.iEventJpaRepository.save(event);

        // Add to Redis
        redisTemplate.opsForZSet().add(EVENTS_KEY, event, event.getId());
    }

    @Override
    public List<Event> findEvents(int offset, int limit) {
        // Get events from Redis
        Set<Event> events = redisTemplate.opsForZSet().range(EVENTS_KEY, offset, offset + limit - 1);

        if (events == null || events.isEmpty()) {
            // If not present in cache, fetch from the database
            List<Event> fetchedEvents = this.findAllByOffsetAndLimit(offset, limit);

            // Add events to Redis (using their IDs as scores)
            for (Event event : fetchedEvents) {
                redisTemplate.opsForZSet().add(EVENTS_KEY, event, event.getId()); // Assuming event.getId() returns a unique score
            }

            return fetchedEvents;
        }

        return List.copyOf(events);
    }

    private List<Event> findAllByOffsetAndLimit(int offset, int limit) {
        String sql = "SELECT TOP :limit * FROM events WHERE id >= :offset";
        Query query = entityManager.createNativeQuery(sql, Event.class);
        query.setParameter("limit", limit);
        query.setParameter("offset", offset);

        return query.getResultList();
    }

    @Transactional
    public void deleteEventsNotInList(List<Long> idsToKeep, String country) {
        // get list of ids from sql database
        String listedIds = idsToKeep.stream()
                .map(String::valueOf)   // Convert each Long to String
                .collect(Collectors.joining(", ", "(", ")"));
        List<Event> events = this.iEventJpaRepository.findIdByEventIdAndCountry(listedIds, country);

        // Convert List to Set for faster lookup
        Set<Long> idsToKeepSet = events.stream().map(Event::getId).collect(Collectors.toSet());
        Set<Event> eventsToDelete = new HashSet<>(); // To collect keys to delete

        // Use RedisTemplate for scanning the sorted set
        Cursor<ZSetOperations.TypedTuple<Event>> cursor = redisTemplate.opsForZSet().scan(
                EVENTS_KEY,
                ScanOptions.scanOptions().match("*").count(1000).build()
        );

        while (cursor.hasNext()) {
            ZSetOperations.TypedTuple<Event> tuple = cursor.next();
            Event element = tuple.getValue(); // Get the member (ID as String)
            if (element != null){
                long id = element.getId(); // Parse the ID

                if (!idsToKeepSet.contains(id)) {
                    eventsToDelete.add(element); // Collect IDs not in the list
                } else {
                    eventsToDelete.remove(element);
                }
            }
        }

        // delete ids in raw events and events
        eventsToDelete.forEach(event -> {
            this.iEventJpaRepository.deleteById(event.getId());
            this.iRawEventJpaRepository.deleteByIdAndCountry(event.getEvent_id(), event.getCountry());
        });

        // Remove keys that are not in the provided list
        if (!eventsToDelete.isEmpty()) {
            redisTemplate.opsForZSet().remove(EVENTS_KEY, eventsToDelete.toArray());
        }
    }

    @Override
    public Long totalNumberOfEvents() {
        return this.redisTemplate.opsForZSet().size(EVENTS_KEY);
    }
}

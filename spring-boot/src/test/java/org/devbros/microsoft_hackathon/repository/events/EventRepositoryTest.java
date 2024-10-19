package org.devbros.microsoft_hackathon.repository.events;

import jakarta.persistence.EntityManager;
import org.devbros.microsoft_hackathon.EmbeddedRedisConfig;
import org.devbros.microsoft_hackathon.RedisConfig;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
@ActiveProfiles("test")
@Import({EmbeddedRedisConfig.class, RedisConfig.class})
public class EventRepositoryTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private IEventJpaRepository iEventJpaRepository;
    @Autowired
    private RedisTemplate<String, Event> redisTemplate;
    @Autowired
    private IRawEventJpaRepository iRawEventJpaRepository;

    private Event event1;
    private Event event2;

    @BeforeEach
    public void setup(){
        event1 = new Event();
        event1.setId(3L);
        event1.setEvent_id("55555");
        event1.setCountry("US");
        event1.setRegion("region");
        event1.setTrailId(1L);
        event1.setDescription("description");
        event1.setPublisherId(1L);
        event2 = new Event();
        event2.setId(4L);
        event2.setEvent_id("66666");
        event2.setCountry("US");
        event2.setRegion("region");
        event2.setTrailId(1L);
        event2.setDescription("description");
        event2.setPublisherId(1L);
    }

    @Test
    public void testThatSavingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, entityManager, iRawEventJpaRepository);

        repository.save(event1);
        repository.save(event2);

        List<Event> results = iEventJpaRepository.findAll();

        Set<Event> events = this.redisTemplate.opsForZSet().range("events", 0, -1);

        assertThat(results.size(), is(2));
        assertThat(events == null, is(false));
        assertThat(events.size(), is(2));

        this.iEventJpaRepository.deleteAll();

        events.forEach(event -> this.redisTemplate.opsForZSet().remove("events", event));
    }

    @Test
    public void testThatFindingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, entityManager, iRawEventJpaRepository);

        this.iEventJpaRepository.save(event1);
        this.iEventJpaRepository.save(event2);

        List<Event> events = repository.findEvents(0, 1);

        Set<Event> redisEvents = this.redisTemplate.opsForZSet().range("events", 0, -1);

        assertThat(events.size(), is(1));
        assertThat(events.get(0).getEvent_id(), is("55555"));

        assertThat(redisEvents == null, is(false));
        assertThat(redisEvents.size(), is(2));

        this.iEventJpaRepository.deleteAll();
        events.forEach(event -> this.redisTemplate.opsForZSet().remove("events", event));
    }

    @Test
    public void testThatDeletingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, entityManager, iRawEventJpaRepository);
        List<Long> ids = Arrays.asList(79L, 80L);

        RawEvent rawEvent1 = new RawEvent();
        rawEvent1.setId(5L);
        rawEvent1.setEventId("55555");
        rawEvent1.setCountry("US");
        RawEvent rawEvent2 = new RawEvent();
        rawEvent2.setId(6L);
        rawEvent2.setEventId("66666");
        rawEvent2.setCountry("US");

        this.iEventJpaRepository.save(event1);
        this.iEventJpaRepository.save(event2);
        this.iRawEventJpaRepository.save(rawEvent1);
        this.iRawEventJpaRepository.save(rawEvent2);

        this.redisTemplate.opsForZSet().add("events", event1, event1.getId());
        this.redisTemplate.opsForZSet().add("events", event2, event2.getId());

        repository.deleteEventsNotInList(ids, "US");

        Set<Event> redisEvents = this.redisTemplate.opsForZSet().range("events", 0, -1);
        List<Event> msEvents = iEventJpaRepository.findAll();
        List<RawEvent> rawEvents = iRawEventJpaRepository.findAll();

        assertThat(redisEvents == null, is(false));
        assertThat(redisEvents.isEmpty(), is(true));
        assertThat(msEvents.isEmpty(), is(true));
        assertThat(rawEvents.isEmpty(), is(true));
    }

    @Test
    public void testCountInCache(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, entityManager, iRawEventJpaRepository);

        this.iEventJpaRepository.save(event1);
        this.iEventJpaRepository.save(event2);

        this.redisTemplate.opsForZSet().add("events", event1, event1.getId());
        this.redisTemplate.opsForZSet().add("events", event2, event2.getId());

        long count = repository.totalNumberOfEvents();

        assertThat(count, is(2L));

        this.iEventJpaRepository.deleteAll();
    }
}

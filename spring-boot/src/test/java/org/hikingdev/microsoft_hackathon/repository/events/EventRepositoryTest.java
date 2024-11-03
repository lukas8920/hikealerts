package org.hikingdev.microsoft_hackathon.repository.events;

import jakarta.persistence.EntityManager;
import org.hikingdev.microsoft_hackathon.EmbeddedRedisConfig;
import org.hikingdev.microsoft_hackathon.RedisConfig;
import org.hikingdev.microsoft_hackathon.event_handling.MapEventMapper;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherJpaRepository;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled
@DataJpaTest
@ActiveProfiles("mock")
@Import({EmbeddedRedisConfig.class, RedisConfig.class})
public class EventRepositoryTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private IEventJpaRepository iEventJpaRepository;
    @Autowired
    private RedisTemplate<String, MapEvent> redisTemplate;
    @Autowired
    private IRawEventJpaRepository iRawEventJpaRepository;
    @Autowired
    private IPublisherJpaRepository iPublisherJpaRepository;
    @Autowired
    private Environment environment;

    private IPublisherRepository iPublisherRepository;
    private MapEventMapper mapEventMapper;
    private Event event1;
    private Event event2;
    private Event event3;

    @BeforeEach
    public void setup(){
        iPublisherRepository = mock(IPublisherRepository.class);
        this.mapEventMapper = new MapEventMapper(environment);

        event1 = new Event();
        event1.setId(1L);
        event1.setEvent_id("55555");
        event1.setCountry("US");
        event1.setRegion("region");
        event1.setDescription("description");
        event1.setPublisherId(1L);
        event1.setTrailIds(Arrays.asList(1L, 2L));
        event2 = new Event();
        event2.setId(2L);
        event2.setEvent_id("66666");
        event2.setCountry("US");
        event2.setRegion("region");
        event2.setDescription("description");
        event2.setPublisherId(1L);
        event2.setTrailIds(Arrays.asList(3L, 4L));
        event3 = new Event();
        event3.setId(3L);
        event3.setEvent_id("79");
        event3.setCountry("US");
        event3.setRegion("region");
        event3.setDescription("description");
        event3.setPublisherId(1L);
        event3.setTrailIds(List.of(1L));
    }

    @Test
    @Disabled
    public void testThatSavingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, mapEventMapper, entityManager, iRawEventJpaRepository, iPublisherRepository);

        when(this.iPublisherRepository.findUserById(any())).thenReturn(new Publisher());

        repository.save(event1);
        repository.save(event2);

        List<Event> results = iEventJpaRepository.findAll();

        Set<MapEvent> events = this.redisTemplate.opsForZSet().range("events", 0, -1);

        assertThat(results.size() >= 2, is(true));
        assertThat(events == null, is(false));
        assertThat(events.size() >= 2, is(true));

        events.forEach(event -> this.redisTemplate.opsForZSet().remove("events", event));
    }

    @Test
    @Disabled
    public void testThatFindingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, mapEventMapper, entityManager, iRawEventJpaRepository, iPublisherRepository);
        Publisher publisher = new Publisher();
        publisher.setId(1L);

        this.iPublisherJpaRepository.save(publisher);

        this.iEventJpaRepository.save(event1);
        this.iEventJpaRepository.save(event2);

        List<MapEvent> events = repository.findEvents(0, 1);

        Set<MapEvent> redisEvents = this.redisTemplate.opsForZSet().range("events", 0, -1);

        assertThat(events.size(), is(1));
        assertThat(events.get(0).getEvent_id(), is("55555"));

        assertThat(redisEvents == null, is(false));
        assertThat(redisEvents.size(), is(1));

        this.iEventJpaRepository.deleteAll();
        events.forEach(event -> this.redisTemplate.opsForZSet().remove("events", event));
    }

    @Test
    @Disabled
    public void testThatSpecificDeletingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, mapEventMapper, entityManager, iRawEventJpaRepository, iPublisherRepository);

        Publisher publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("test");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("55555L");
        openAiEvent.setCountry("ZZ");
        Event event = new Event();
        event.setEvent_id("55555L");
        event.setCountry("ZZ");
        event.setPublisherId(1L);
        MapEvent mapEvent = new MapEvent();
        mapEvent.setCountry("ZZ");
        mapEvent.setEvent_id("55555L");
        mapEvent.setPublisher(publisher.getName());
        mapEvent.setPublisherId(event.getPublisherId());

        Event tmpEvent = this.iEventJpaRepository.save(event);
        this.iPublisherJpaRepository.save(publisher);

        mapEvent.setId(tmpEvent.getId());
        redisTemplate.opsForZSet().add("events", mapEvent, tmpEvent.getId());

        repository.deleteByOpenAiEvent(openAiEvent);

        // check that databases are empty
        List<Event> events = this.iEventJpaRepository.findAll();
        Set<MapEvent> redisEvents = this.redisTemplate.opsForZSet().range("events", 0, -1);

        assertThat(events.isEmpty(), is(true));
        assertThat(redisEvents != null, is(true));
        assertThat(redisEvents.isEmpty(), is(true));
    }

    @Test
    @Disabled
    public void testThatDeletingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository, redisTemplate, mapEventMapper, entityManager, iRawEventJpaRepository, iPublisherRepository);
        List<String> ids = Arrays.asList("79", "80");

        Publisher publisher = new Publisher();
        publisher.setId(1L);

        RawEvent rawEvent1 = new RawEvent();
        rawEvent1.setId(1L);
        rawEvent1.setEventId("55555");
        rawEvent1.setCountry("US");
        RawEvent rawEvent2 = new RawEvent();
        rawEvent2.setId(2L);
        rawEvent2.setEventId("66666");
        rawEvent2.setCountry("US");
        RawEvent rawEvent3 = new RawEvent();
        rawEvent3.setId(3L);
        rawEvent3.setEventId("79");
        rawEvent3.setCountry("US");

        MapEvent mapEvent1 = new MapEvent();
        mapEvent1.setId(1L);
        mapEvent1.setEvent_id("55555");
        mapEvent1.setCountry("US");
        mapEvent1.setDescription("description");
        mapEvent1.setPublisherId(1L);
        MapEvent mapEvent2 = new MapEvent();
        mapEvent2.setId(2L);
        mapEvent2.setEvent_id("66666");
        mapEvent2.setCountry("US");
        mapEvent2.setDescription("description");
        mapEvent2.setPublisherId(1L);
        MapEvent mapEvent3 = new MapEvent();
        mapEvent3.setId(3L);
        mapEvent3.setEvent_id("79");
        mapEvent3.setCountry("US");
        mapEvent3.setDescription("description");
        mapEvent3.setPublisherId(1L);

        this.iPublisherJpaRepository.save(publisher);

        this.iEventJpaRepository.save(event1);
        this.iEventJpaRepository.save(event2);
        this.iEventJpaRepository.save(event3);
        this.iRawEventJpaRepository.save(rawEvent1);
        this.iRawEventJpaRepository.save(rawEvent2);
        this.iRawEventJpaRepository.save(rawEvent3);

        this.redisTemplate.opsForZSet().add("events", mapEvent1, mapEvent1.getId());
        this.redisTemplate.opsForZSet().add("events", mapEvent2, mapEvent2.getId());
        this.redisTemplate.opsForZSet().add("events", mapEvent3, mapEvent3.getId());

        repository.deleteEventsNotInList(ids, "US");

        Set<MapEvent> redisEvents = this.redisTemplate.opsForZSet().range("events", 0, -1);
        List<Event> msEvents = iEventJpaRepository.findAll();
        List<RawEvent> rawEvents = iRawEventJpaRepository.findAll();

        assertThat(redisEvents == null, is(false));
        assertThat(redisEvents.size(), is(1));
        assertThat(msEvents.size(), is(1));
        assertThat(rawEvents.size(), is(1));
    }
}
package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_injection.entities.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@DataJpaTest
@ActiveProfiles("mock")
public class EventRepositoryTest {
    @Autowired
    private IEventJpaRepository iEventJpaRepository;

    @Test
    public void testThatSavingWorks(){
        EventRepository repository = new EventRepository(iEventJpaRepository);
        Event event1 = new Event();
        event1.setEvent_id("55555");
        event1.setCountry("US");
        event1.setRegion("region");
        event1.setTrailId(1L);
        event1.setDescription("description");
        event1.setPublisherId(1L);
        Event event2 = new Event();
        event2.setEvent_id("66666");
        event2.setCountry("US");
        event2.setRegion("region");
        event2.setTrailId(1L);
        event2.setDescription("description");
        event2.setPublisherId(1L);
        List<Event> events = Arrays.asList(event1, event2);

        repository.save(events);

        List<Event> results = iEventJpaRepository.findAll();

        assertThat(results.size(), is(2));
    }
}

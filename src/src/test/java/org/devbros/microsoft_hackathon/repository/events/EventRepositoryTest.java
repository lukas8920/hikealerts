package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_injection.entities.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
        Event event2 = new Event();
        List<Event> events = Arrays.asList(event1, event2);

        repository.save(events);

        List<Event> results = iEventJpaRepository.findAll();

        assertThat(results.size(), is(2));
    }
}

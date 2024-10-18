package org.devbros.microsoft_hackathon.repository.raw_events;

import org.devbros.microsoft_hackathon.event_injection.entities.RawEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@DataJpaTest
@ActiveProfiles("mock")
public class RawEventRepositoryTest {
    @Autowired
    private IRawEventJpaRepository jpaRepository;

    private RawEventRepository repository;

    @BeforeEach
    public void setup(){
        repository = new RawEventRepository(jpaRepository);
    }

    @Test
    public void testFindingEvent(){
        RawEvent rawEvent1 = new RawEvent();
        rawEvent1.setId(1L);
        rawEvent1.setEventId("1");
        rawEvent1.setCountry("US");
        RawEvent rawEvent2 = new RawEvent();
        rawEvent2.setId(2L);
        rawEvent2.setEventId("2");
        rawEvent2.setCountry("US");

        this.jpaRepository.save(rawEvent1);
        this.jpaRepository.save(rawEvent2);

        RawEvent rawEvent = this.repository.findRawEvent("2", "US");

        assertThat(rawEvent.getEventId(), is(rawEvent2.getEventId()));
        assertThat(rawEvent.getCountry(), is(rawEvent2.getCountry()));
    }

    @Test
    public void testFindingReturnsNull(){
        RawEvent rawEvent1 = new RawEvent();
        rawEvent1.setId(1L);
        rawEvent1.setEventId("1");
        rawEvent1.setCountry("US");

        this.jpaRepository.save(rawEvent1);

        RawEvent rawEvent = this.repository.findRawEvent("2", "US");

        assertThat(rawEvent, nullValue());
    }
}

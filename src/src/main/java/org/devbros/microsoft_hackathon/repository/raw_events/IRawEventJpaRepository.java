package org.devbros.microsoft_hackathon.repository.raw_events;

import org.devbros.microsoft_hackathon.event_injection.entities.RawEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRawEventJpaRepository extends JpaRepository<RawEvent, Long> {
    RawEvent findFirstByEventIdAndCountry(String eventId, String country);
}

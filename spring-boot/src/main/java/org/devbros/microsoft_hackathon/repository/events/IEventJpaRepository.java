package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_injection.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IEventJpaRepository extends JpaRepository<Event, Long> {
}

package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_injection.entities.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventRepository implements IEventRepository {
    private final IEventJpaRepository iEventJpaRepository;

    @Autowired
    public EventRepository(IEventJpaRepository iEventJpaRepository){
        this.iEventJpaRepository = iEventJpaRepository;
    }

    @Override
    public void save(List<Event> events) {
        this.iEventJpaRepository.saveAll(events);
    }
}

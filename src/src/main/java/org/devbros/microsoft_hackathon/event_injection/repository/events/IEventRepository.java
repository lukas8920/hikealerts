package org.devbros.microsoft_hackathon.event_injection.repository.events;

import org.devbros.microsoft_hackathon.event_injection.entities.Event;

import java.util.List;

public interface IEventRepository {
    void save(List<Event> event);
}

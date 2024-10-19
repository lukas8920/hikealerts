package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;

import java.util.List;

public interface IEventRepository {
    void save(Event event);
    List<Event> findEvents(int offset, int limit);
    void deleteEventsNotInList(List<Long> idsToKeep, String country);
    Long totalNumberOfEvents();
}

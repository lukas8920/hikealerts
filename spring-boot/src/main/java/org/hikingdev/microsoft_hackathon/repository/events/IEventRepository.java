package org.hikingdev.microsoft_hackathon.repository.events;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;

import java.util.List;

public interface IEventRepository {
    void save(Event event);
    List<MapEvent> findEvents(int offset, int limit);
    void deleteEventsNotInList(List<String> idsToKeep, String country);
    void deleteByOpenAiEvent(OpenAiEvent openAiEvent);
}

package org.hikingdev.microsoft_hackathon.repository.events;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface IEventRepository {
    void save(Event event);
    List<MapEvent> findEvents(int offset, int limit);
    Set<MapEvent> deleteEventsNotInList(List<String> idsToKeep, String country);
    void deleteByOpenAiEvent(OpenAiEvent openAiEvent);
    List<EventResponse> queryEvents(Double[] boundaries, String country, LocalDate fromDate, LocalDate toDate, LocalDate createDate, String createdBy, boolean nullDates, int limit, int offset);
    boolean deleteByIdAndPublisher(Long eventId, Long publisherId);
    List<MapEvent> refreshCache();
    List<MapEvent> findEventsByTrailAndCountry(String trail, String country);
}

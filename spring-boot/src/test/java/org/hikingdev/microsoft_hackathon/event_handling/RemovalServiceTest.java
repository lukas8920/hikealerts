package org.hikingdev.microsoft_hackathon.event_handling;

import com.azure.storage.queue.QueueClient;
import org.hikingdev.microsoft_hackathon.event_handling.RemovalService;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.map_layer.TileVectorService;
import org.hikingdev.microsoft_hackathon.repository.events.EventRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.util.EventNotFoundException;
import org.hikingdev.microsoft_hackathon.util.InvalidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemovalServiceTest {
    private RemovalService removalService;
    private EventRepository eventRepository;

    @BeforeEach
    public void setup(){
        ITrailRepository iTrailRepository = mock(ITrailRepository.class);
        TileVectorService tileVectorService = mock(TileVectorService.class);
        QueueClient queueClient = mock(QueueClient.class);

        this.eventRepository = mock(EventRepository.class);
        this.removalService = new RemovalService(eventRepository, queueClient, iTrailRepository, tileVectorService);
    }

    @Test
    public void testSuccessfulRemoval() throws EventNotFoundException, InvalidationException {
        MapEvent mapEvent = new MapEvent();
        mapEvent.setPublisherId(1L);
        mapEvent.setId(1L);

        when(eventRepository.findEventsByTrailAndCountry("dummy", "ZZ")).thenReturn(List.of(mapEvent));
        when(eventRepository.deleteByIdAndPublisher(1L, 1L)).thenReturn(true);

        this.removalService.removeEvent("dummy", "ZZ", 1L);
    }

    @Test
    public void testNoEventsFound(){
        when(eventRepository.findEventsByTrailAndCountry("dummy", "DD")).thenReturn(new ArrayList<>());

        Exception e = assertThrows(EventNotFoundException.class, () -> {
            this.removalService.removeEvent("dummy", "DD", 1L);
            throw new IllegalArgumentException("Invalid argument");
        });

        assertThat(e.getMessage(), is("No events found for dummy, DD"));
    }

    @Test
    public void testDeletionFail(){
        MapEvent mapEvent = new MapEvent();
        mapEvent.setPublisherId(1L);
        mapEvent.setId(1L);

        when(eventRepository.findEventsByTrailAndCountry("dummy", "DD")).thenReturn(List.of(mapEvent));
        when(eventRepository.deleteByIdAndPublisher(1L, 1L)).thenReturn(false);

        Exception e = assertThrows(EventNotFoundException.class, () -> {
            this.removalService.removeEvent("dummy", "DD", 1L);
            throw new IllegalArgumentException("Invalid argument");
        });

        assertThat(e.getMessage(), is("Event deletion for 1 not possible."));
    }

    @Test
    public void testUnauthorizedDeletion(){
        MapEvent mapEvent = new MapEvent();
        mapEvent.setPublisherId(1L);
        mapEvent.setId(1L);

        when(eventRepository.findEventsByTrailAndCountry("dummy", "DD")).thenReturn(List.of(mapEvent));
        when(eventRepository.deleteByIdAndPublisher(1L, 1L)).thenReturn(false);

        Exception e = assertThrows(InvalidationException.class, () -> {
            this.removalService.removeEvent("dummy", "DD", 2L);
            throw new IllegalArgumentException("Invalid argument");
        });

        assertThat(e.getMessage(), is("User is not authorized to delete events."));
    }
}

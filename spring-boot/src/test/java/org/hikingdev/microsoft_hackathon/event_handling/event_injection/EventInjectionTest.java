package org.hikingdev.microsoft_hackathon.event_handling.event_injection;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.map_layer.MapLayerService;
import org.hikingdev.microsoft_hackathon.map_layer.TileVectorService;
import org.hikingdev.microsoft_hackathon.repository.events.EventRepository;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class EventInjectionTest {
    private static EventInjection eventInjection;
    private static MapLayerService mapLayerService;
    private static TileVectorService tileVectorService;
    private static IEventRepository iEventRepository;
    private static ITrailRepository iTrailRepository;

    @BeforeAll
    public static void setup(){
        iEventRepository = mock(EventRepository.class);
        mapLayerService = mock(MapLayerService.class);
        tileVectorService = mock(TileVectorService.class);
        iTrailRepository = mock(ITrailRepository.class);
        eventInjection = new EventInjection(null, iEventRepository, tileVectorService, iTrailRepository, null, mapLayerService);
    }

    @Test
    public void testInvalidCountry(){
        OpenAiEvent openAiEvent1 = new OpenAiEvent();
        openAiEvent1.setFromDate("12/05/2024 12:12:12");
        openAiEvent1.setToDate("12/05/2024");
        openAiEvent1.setCountry("USA");
        openAiEvent1.setRegion("region");
        OpenAiEvent openAiEvent2 = new OpenAiEvent();
        openAiEvent2.setFromDate("12/05/2024 12:12:12");
        openAiEvent2.setToDate("12/05/2024");
        openAiEvent2.setCountry(null);
        openAiEvent2.setRegion("region");
        List<OpenAiEvent> openAiEvents = Arrays.asList(openAiEvent1, openAiEvent2);

        List<Message> messages = eventInjection.injectEvent(openAiEvents);

        assertThat(messages.size(), is(2));
    }

    @Test
    public void testSuccessfulTrails(){
        OpenAiEvent openAiEvent1 = new OpenAiEvent();
        openAiEvent1.setEventId("1L");
        openAiEvent1.setCountry("DD");
        OpenAiEvent openAiEvent2 = new OpenAiEvent();
        openAiEvent2.setEventId("2L");
        openAiEvent2.setCountry("DD");
        List<OpenAiEvent> openAiEvents = List.of(openAiEvent1, openAiEvent2);

        Message message = new Message();
        message.setId("2L");
        message.setCountry("DD");
        List<Message> messages = List.of(message);

        Trail trail = new Trail();
        trail.setCoordinates(new byte[]{1});
        List<Trail> trails = List.of(trail);

        when(iTrailRepository.findTrailsByEventIdAndCountry("1L", "DD")).thenReturn(trails);

        List<byte[]> bytes = eventInjection.successfulTrails(openAiEvents, messages);

        assertThat(bytes.size(), is(1));
        assertThat(bytes.get(0).length, is(1));
    }
}

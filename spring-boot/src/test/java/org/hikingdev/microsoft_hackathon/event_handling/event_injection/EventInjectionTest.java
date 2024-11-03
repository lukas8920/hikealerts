package org.hikingdev.microsoft_hackathon.event_handling.event_injection;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries.USInjector;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.map_layer.MapLayerService;
import org.hikingdev.microsoft_hackathon.repository.events.EventRepository;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class EventInjectionTest {
    private static EventInjection eventInjection;
    private static MapLayerService mapLayerService;

    private static IEventRepository iEventRepository;

    @BeforeAll
    public static void setup(){
        iEventRepository = mock(EventRepository.class);
        mapLayerService = mock(MapLayerService.class);
        eventInjection = new EventInjection(null, iEventRepository, null, null, mapLayerService);
    }

    @Test
    public void testValidDateTimeFormats() throws ParseException {
        EventInjection injection = spy(new EventInjection(null, iEventRepository, null, null, mapLayerService));
        USInjector usInjector = mock(USInjector.class);

        OpenAiEvent openAiEvent1 = new OpenAiEvent();
        openAiEvent1.setFromDate("12/05/2024 12:12:12");
        openAiEvent1.setToDate("12/05/2024");
        openAiEvent1.setCountry("US");
        openAiEvent1.setRegion("region");
        OpenAiEvent openAiEvent2 = new OpenAiEvent();
        openAiEvent2.setFromDate(null);
        openAiEvent2.setToDate("YYYY");
        openAiEvent2.setCountry("US");
        openAiEvent2.setRegion("region");
        OpenAiEvent openAiEvent3 = new OpenAiEvent();
        openAiEvent3.setFromDate("12/05/YYYY");
        openAiEvent3.setToDate("12/05/2024 12:12:12");
        openAiEvent3.setCountry("US");
        openAiEvent3.setRegion("region");
        List<OpenAiEvent> openAiEvents = Arrays.asList(openAiEvent1, openAiEvent2, openAiEvent3);

        doReturn(usInjector).when(injection).assignCountryInjector(openAiEvent1);
        doReturn(usInjector).when(injection).assignCountryInjector(openAiEvent2);
        doReturn(usInjector).when(injection).assignCountryInjector(openAiEvent3);
        when(usInjector.matchTrails(openAiEvent1)).thenReturn(true);
        when(usInjector.matchTrails(openAiEvent2)).thenReturn(true);
        when(usInjector.matchTrails(openAiEvent3)).thenReturn(true);

        List<Message> messages = injection.injectEvent(openAiEvents);

        assertThat(messages.get(0).getMessage(), is("All events processed."));
    }

    @Test
    public void testInvalidDateTimeFormats(){
        OpenAiEvent openAiEvent1 = new OpenAiEvent();
        openAiEvent1.setFromDate("12/05/2024 12:12:12.333");
        OpenAiEvent openAiEvent2 = new OpenAiEvent();
        openAiEvent2.setToDate("05/YYYY");
        OpenAiEvent openAiEvent3 = new OpenAiEvent();
        openAiEvent3.setFromDate("12/05/2024 12:12");
        OpenAiEvent openAiEvent4 = new OpenAiEvent();
        openAiEvent4.setToDate("12.12.2024");
        OpenAiEvent openAiEvent5 = new OpenAiEvent();
        openAiEvent5.setFromDate("12/kk/2024");
        OpenAiEvent openAiEvent6 = new OpenAiEvent();
        openAiEvent6.setToDate("12/05/24");
        List<OpenAiEvent> openAiEvents = Arrays.asList(openAiEvent1, openAiEvent2, openAiEvent3, openAiEvent4, openAiEvent5, openAiEvent6);

        List<Message> messages = eventInjection.injectEvent(openAiEvents);

        assertThat(messages.size(), is(6));
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
    public void testInvalidRegion() {
        OpenAiEvent openAiEvent1 = new OpenAiEvent();
        openAiEvent1.setFromDate("12/05/2024 12:12:12");
        openAiEvent1.setToDate("12/05/2024");
        openAiEvent1.setCountry("US");
        openAiEvent1.setRegion("");
        OpenAiEvent openAiEvent2 = new OpenAiEvent();
        openAiEvent2.setFromDate("12/05/2024 12:12:12");
        openAiEvent2.setToDate("12/05/2024");
        openAiEvent2.setCountry("US");
        openAiEvent2.setRegion(null);
        List<OpenAiEvent> openAiEvents = Arrays.asList(openAiEvent1, openAiEvent2);

        List<Message> messages = eventInjection.injectEvent(openAiEvents);

        assertThat(messages.size(), is(2));
    }
}
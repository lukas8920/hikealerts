package org.hikingdev.microsoft_hackathon.event_handling.event_injection;

import org.hikingdev.microsoft_hackathon.event_handling.EventService;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.RawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.users.IUserRepository;
import org.hikingdev.microsoft_hackathon.user.entities.MessageResponse;
import org.hikingdev.microsoft_hackathon.util.AiException;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EventServiceTest {
    private static EventService eventService;

    private IEventRepository iEventRepository;
    private OpenAiService openAiService;
    private IEventInjection iEventInjection;

    @BeforeEach
    public void setup(){
        iEventRepository = mock(IEventRepository.class);
        IUserRepository iUserRepository = mock(IUserRepository.class);
        iEventInjection = mock(IEventInjection.class);
        openAiService = mock(OpenAiService.class);
        RawEventRepository rawEventRepository = mock(RawEventRepository.class);
        eventService = new EventService(iEventRepository, iUserRepository, openAiService, rawEventRepository, iEventInjection);
    }

    @Test
    public void testPullRequestInvalidBoundary(){
        String boundary = "1.234,0.234,4.567";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.requestEvents(boundary, null, null, null, null, false, "All", 0, 0));

        assertThat(e.getMessage(), is("Provided boundaries are not valid."));
    }

    @Test
    public void testPullRequestValidBoundary() throws BadRequestException {
        String boundary = "1.234,0.234,4.567,4.789";
        Double[] dblBoundary = new Double[]{1.234, 0.234, 4.567, 4.789};
        when(iEventRepository.queryEvents(dblBoundary, null, null, null, null, "All", false, 0, 0)).thenReturn(List.of(new EventResponse()));

        List<EventResponse> eventResponses = eventService.requestEvents(boundary, null, null, null, null, false, "All", 0, 0);

        assertThat(eventResponses.size(), is(1));
    }

    @Test
    public void testPullRequestInvalidDate(){
        String country = "NZ";
        LocalDate fromDate = LocalDate.of(2024, 11, 6);
        LocalDate toDate = LocalDate.of(2024, 11, 5);

        Exception e = assertThrows(BadRequestException.class, () -> eventService.requestEvents(null, country, fromDate, toDate, null, false, "All", 0, 0));

        assertThat(e.getMessage(), is("To date needs to be greater than the from date."));
    }

    @Test
    public void testPullRequestEitherCountryOrBoundary(){
        String country = null;
        String boundary = null;

        Exception e = assertThrows(BadRequestException.class, () -> eventService.requestEvents(boundary, country, null, null, null, false, "All", 0, 0));

        assertThat(e.getMessage(), is("Either country or boundary needs to be provided."));
    }

    @Test
    public void testPullRequestInvalidLimit(){
        int limit = 110;
        String country = "NZ";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.requestEvents(null, country, null, null, null, false, "All", 0, limit));

        assertThat(e.getMessage(), is("Maximum 100 events can be returned per request."));
    }

    @Test
    public void testPullRequestInvalidCountry(){
        String country = "PL";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.requestEvents(null, country, null, null, null, false, "All", 0, 0));

        assertThat(e.getMessage(), is("Country PL is currently not supported."));
    }

    @Test
    public void testPullRequestValidCreatedBys() throws BadRequestException {
        String country = "NZ";

        when(iEventRepository.queryEvents(new Double[]{}, country, null, null, null, "Community", false, 0, 0)).thenReturn(List.of(new EventResponse()));
        List<EventResponse> eventResponses1 = eventService.requestEvents(null, country, null, null, null, false, "Community", 0, 0);
        assertThat(eventResponses1.size(), is(1));

        when(iEventRepository.queryEvents(new Double[]{}, country, null, null, null, "Official", false, 0, 0)).thenReturn(List.of(new EventResponse()));
        List<EventResponse> eventResponses2 = eventService.requestEvents(null, country, null, null, null, false, "Official", 0, 0);
        assertThat(eventResponses2.size(), is(1));
    }

    @Test
    public void testPullRequestInvalidCreatedBys(){
        String createdBy = "everything";
        String country = "NZ";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.requestEvents(null, country, null, null, null, false, createdBy, 0, 0));

        assertThat(e.getMessage(), is("Valid values for createdBy are [All, Community, Official]"));
    }

    @Test
    public void testPublishInvalidCountry(){
        String country = "PL";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.publishEvent(country, "dummy", "dummy", LocalDate.of(2024, 11, 5), LocalDate.of(2024, 11, 6)));

        assertThat(e.getMessage(), is("Country PL is currently not supported."));
    }

    @Test
    public void testPublishValidDescriptionAndTitle() throws AiException, BadRequestException {
        String description = "dummy description";
        String title = "dummy title";
        String country = "NZ";

        OpenAiEvent openAiEvent = new OpenAiEvent();
        when(openAiService.sendOpenAiRequest(any())).thenReturn(openAiEvent);
        when(iEventInjection.injectEvent(List.of(openAiEvent))).thenReturn(List.of(new Message("1L", "xx", "test")));

        MessageResponse message = eventService.publishEvent(country, title, description, null, null);

        assertThat(message.getMessage(), is("test"));
    }

    @Test
    public void testPublishInvalidDescription(){
        String description = "x".repeat(2001);
        String title = "dummy title";
        String country = "NZ";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.publishEvent(country, title, description, null, null));

        assertThat(e.getMessage(), is("Description length cannot exceed 2000 characters"));
    }

    @Test
    public void testPublishInvalidTitle(){
        String description = "dummy description";
        String title = "x".repeat(201);
        String country = "NZ";

        Exception e = assertThrows(BadRequestException.class, () -> eventService.publishEvent(country, title, description, null, null));

        assertThat(e.getMessage(), is("Title length cannot exceed 200 characters."));
    }
}

package org.hikingdev.microsoft_hackathon.event_handling;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.user.entities.MessageResponse;
import org.hikingdev.microsoft_hackathon.util.AiException;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class.getName());

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @CrossOrigin
    @GetMapping("/pull")
    public ResponseEntity<List<MapEvent>> getEventData(@RequestParam int offset, @RequestParam int limit) throws BadRequestException {
        logger.info("Request event data - offset: " + offset + " - limit: " + limit);
        List<MapEvent> mapEvents = this.eventService.pullData(offset, limit);
        return ResponseEntity.ok(mapEvents);
    }

    @CrossOrigin
    @GetMapping("/query")
    public ResponseEntity<List<EventResponse>> queryEvents(@RequestParam(name = "boundary", required = false) String boundary, @RequestParam(name = "country", required = false) String country,
                                                           @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate, @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                           @RequestParam(name = "createDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createDate, @RequestParam(name = "nullDates", required = false) boolean nullDates,
                                                           @RequestParam(name = "createdBy", required = false, defaultValue = "all") String createdBy,
                                                           @RequestParam(name = "offset", required = false, defaultValue = "0") int offset, @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) throws BadRequestException {
        List<EventResponse> events = this.eventService.requestEvents(boundary, country, fromDate, toDate, createDate,
                nullDates, createdBy, offset, limit);
        return ResponseEntity.ok(events);
    }

    @CrossOrigin
    @PostMapping("/publish")
    public ResponseEntity<MessageResponse> publishEvent(@RequestParam(name = "title", required = true) String title, @RequestParam(name = "description", required = true) String description, @RequestParam(name = "country", required = true) String country,
                                                        @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate, @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws BadRequestException, AiException {
        MessageResponse messageResponse = this.eventService.publishEvent(country, title, description, fromDate, toDate);
        return ResponseEntity.ok(messageResponse);
    }

    @CrossOrigin
    @PostMapping("/delete")
    public ResponseEntity<MessageResponse> deleteEvent(@RequestParam(name = "id", required = true) Long id) throws BadRequestException {
        MessageResponse messageResponse = this.eventService.deleteEvent(id);
        return ResponseEntity.ok(messageResponse);
    }
}

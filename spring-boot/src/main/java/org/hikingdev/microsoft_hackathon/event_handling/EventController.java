package org.hikingdev.microsoft_hackathon.event_handling;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.IEventInjection;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class.getName());

    private final EventProviderService eventProviderService;

    @Autowired
    public EventController(EventProviderService eventProviderService){
        this.eventProviderService = eventProviderService;
    }

    @CrossOrigin
    @GetMapping("/pull")
    public ResponseEntity<List<MapEvent>> getEventData(@RequestParam int offset, @RequestParam int limit){
        logger.info("Request event data - offset: " + offset + " - limit: " + limit);
        List<MapEvent> mapEvents = this.eventProviderService.pullData(offset, limit);
        return ResponseEntity.ok(mapEvents);
    }
}

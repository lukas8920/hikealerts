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

    private final IEventInjection iEventInjection;
    private final EventProviderService eventProviderService;

    @Autowired
    public EventController(IEventInjection iEventInjection, EventProviderService eventProviderService){
        this.eventProviderService = eventProviderService;
        this.iEventInjection = iEventInjection;
    }

    @PostMapping("/inject")
    public ResponseEntity<List<Message>> injectOpenAiEvents(@RequestBody List<OpenAiEvent> openAiEvents) {
        logger.debug("Received request");
        List<Message> messages = this.iEventInjection.injectEvent(openAiEvents);
        return ResponseEntity.ok(messages);
    }

    @CrossOrigin
    @GetMapping("/pull")
    public ResponseEntity<List<MapEvent>> getEventData(@RequestParam int offset, @RequestParam int limit){
        logger.info("Request event data - offset: " + offset + " - limit: " + limit);
        List<MapEvent> mapEvents = this.eventProviderService.pullData(offset, limit);
        return ResponseEntity.ok(mapEvents);
    }
}

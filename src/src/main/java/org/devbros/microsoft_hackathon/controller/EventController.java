package org.devbros.microsoft_hackathon.controller;

import org.devbros.microsoft_hackathon.event_injection.IEventInjection;
import org.devbros.microsoft_hackathon.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_injection.entities.OpenAiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class.getName());

    private final IEventInjection iEventInjection;

    @Autowired
    public EventController(IEventInjection iEventInjection){
        this.iEventInjection = iEventInjection;
    }

    @PostMapping("/inject")
    public ResponseEntity<List<Message>> injectOpenAiEvents(@RequestBody List<OpenAiEvent> openAiEvents) {
        logger.debug("Received request");
        List<Message> messages = this.iEventInjection.injectEvent(openAiEvents);
        return ResponseEntity.ok(messages);
    }
}

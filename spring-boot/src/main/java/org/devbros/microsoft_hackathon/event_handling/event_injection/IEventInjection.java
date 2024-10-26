package org.devbros.microsoft_hackathon.event_handling.event_injection;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;

import java.util.List;

public interface IEventInjection {
    List<Message> injectEvent(List<OpenAiEvent> openAiEvents);
}

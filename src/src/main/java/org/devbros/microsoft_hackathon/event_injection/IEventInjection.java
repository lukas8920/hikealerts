package org.devbros.microsoft_hackathon.event_injection;

import org.devbros.microsoft_hackathon.BadRequestException;
import org.devbros.microsoft_hackathon.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_injection.entities.OpenAiEvent;

import java.util.List;

public interface IEventInjection {
    Message injectEvent(List<OpenAiEvent> openAiEvents) throws BadRequestException;
}

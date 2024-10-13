package org.devbros.microsoft_hackathon.event_injection;

import org.devbros.microsoft_hackathon.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_injection.entities.RawEvent;

import java.util.List;

public interface IEventInjection {
    Message injectEvent(List<RawEvent> rawEvents);
}

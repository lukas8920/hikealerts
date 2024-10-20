package org.devbros.microsoft_hackathon.repository.raw_events;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;

public interface IRawEventRepository {
    RawEvent findRawEvent(String eventId, String countryCode);
}
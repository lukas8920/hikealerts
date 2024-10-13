package org.devbros.microsoft_hackathon.event_injection.repository.raw_events;

import org.devbros.microsoft_hackathon.event_injection.entities.RawEvent;

public interface IRawEventRepository {
    RawEvent findRawEvent(String eventId, String countryCode);
}

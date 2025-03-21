package org.hikingdev.microsoft_hackathon.repository.raw_events;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RawEventRepository implements IRawEventRepository {
    private final IRawEventJpaRepository IRawEventJpaRepository;

    @Autowired
    public RawEventRepository(IRawEventJpaRepository IRawEventJpaRepository){
        this.IRawEventJpaRepository = IRawEventJpaRepository;
    }

    @Override
    public RawEvent findRawEvent(String eventId, String countryCode) {
        return this.IRawEventJpaRepository.findFirstByEventIdAndCountry(eventId, countryCode);
    }

    @Override
    public void save(RawEvent rawEvent) {
        this.IRawEventJpaRepository.save(rawEvent);
    }
}

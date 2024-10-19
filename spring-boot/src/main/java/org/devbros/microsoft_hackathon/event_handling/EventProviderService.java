package org.devbros.microsoft_hackathon.event_handling;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvents;
import org.devbros.microsoft_hackathon.repository.events.IEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventProviderService {
    private final IEventRepository iEventRepository;

    @Autowired
    public EventProviderService(IEventRepository iEventRepository){
        this.iEventRepository = iEventRepository;
    }

    public MapEvents pullData(int offset, int limit) {
        List<Event> events = this.iEventRepository.findEvents(offset, limit);
        long total = this.iEventRepository.totalNumberOfEvents();

        MapEvents mapEvents = new MapEvents();
        mapEvents.setEvents(events);
        mapEvents.setTotal(total);

        return mapEvents;
    }
}

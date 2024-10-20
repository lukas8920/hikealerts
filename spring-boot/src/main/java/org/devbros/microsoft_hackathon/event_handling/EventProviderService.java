package org.devbros.microsoft_hackathon.event_handling;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
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

    public List<MapEvent> pullData(int offset, int limit) {
        return this.iEventRepository.findEvents(offset, limit);
    }
}

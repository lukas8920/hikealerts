package org.hikingdev.microsoft_hackathon.event_handling;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
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

    public List<MapEvent> pullData(int offset, int limit) throws BadRequestException {
        if (limit > 100){
            throw new BadRequestException("Maximum 100 events can be fetched via this endpoint.");
        }
        return this.iEventRepository.findEvents(offset, limit);
    }
}

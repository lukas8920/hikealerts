package org.hikingdev.microsoft_hackathon.event_handling;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Coordinate;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventResponseMapper {
    public EventResponse map(Object[] object){
        EventResponse eventResponse = new EventResponse();
        eventResponse.setId((Long) object[0]);
        eventResponse.setCountry((String) object[1]);
        eventResponse.setTrailname((String) object[2]);
        eventResponse.setTrail_id((Long) object[3]);
        eventResponse.setTitle((String) object[4]);
        eventResponse.setDescription((String) object[5]);
        eventResponse.setFromDate((LocalDateTime) object[6]);
        eventResponse.setToDate((LocalDateTime) object[7]);
        eventResponse.setCrDate((LocalDateTime) object[8]);
        eventResponse.setPublisher((String) object[9]);
        eventResponse.setStatus(mapStatus((Status) object[10]));
        eventResponse.setTrailMidPoint(new Coordinate((double) object[11], (double) object[12]));
        return eventResponse;
    }

    String mapStatus(Status status){
        if (status.equals(Status.COMMUNITY)){
            return "Community";
        } else {
            return "Official";
        }
    }
}

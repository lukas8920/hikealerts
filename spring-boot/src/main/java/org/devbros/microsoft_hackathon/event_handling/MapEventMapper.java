package org.devbros.microsoft_hackathon.event_handling;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.devbros.microsoft_hackathon.publisher_management.entities.Publisher;
import org.devbros.microsoft_hackathon.publisher_management.entities.Status;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class MapEventMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public MapEvent map (Event event, Publisher publisher){
        MapEvent mapEvent = new MapEvent();
        mapEvent.setId(event.getId());
        mapEvent.setTitle(event.getTitle());
        mapEvent.setDescription(event.getDescription());
        mapEvent.setPublisher(publisher.getName());
        mapEvent.setStatus(this.mapStatus(publisher.getStatus()));
        if (event.getCreateDatetime() != null) {
            mapEvent.setCreateDate(event.getCreateDatetime().format(formatter));
        }
        mapEvent.setLat(event.getMidLatitudeCoordinate());
        mapEvent.setLng(event.getMidLongitudeCoordinate());
        mapEvent.setEvent_id(event.getEvent_id());
        mapEvent.setCountry(event.getCountry());
        return mapEvent;
    }

    String mapStatus(Status status){
        if (Status.OFFICIAL.equals(status)){
            return "Official";
        } else {
            return "Community";
        }
    }
}

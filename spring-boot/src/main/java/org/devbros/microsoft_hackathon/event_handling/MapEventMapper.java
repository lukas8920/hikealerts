package org.devbros.microsoft_hackathon.event_handling;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.devbros.microsoft_hackathon.publisher_management.entities.Publisher;
import org.devbros.microsoft_hackathon.publisher_management.entities.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class MapEventMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Environment environment;
    @Autowired
    public MapEventMapper(Environment environment){
        this.environment = environment;
    }

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
        mapEvent.setUrl(event.getUrl());
        mapEvent.setTrailIds(event.getTrailIds());
        return mapEvent;
    }

    public MapEvent map(Object[] object) {
        MapEvent mapEvent = new MapEvent();
        mapEvent.setId((Long) object[0]);
        mapEvent.setTitle((String) object[1]);
        mapEvent.setDescription((String) object[2]);
        mapEvent.setPublisher((String) object[3]);
        // handle jpa ms sql bug
        if (this.environment.getActiveProfiles()[0].equals("prod")){
            mapEvent.setStatus(mapStatus((Status) object[4]));
        } else {
            mapEvent.setStatus((String) object[4]);
        }
        mapEvent.setCreateDate((String) object[5]);
        mapEvent.setLat((double) object[6]);
        mapEvent.setLng((double) object[7]);
        mapEvent.setEvent_id((String) object[8]);
        mapEvent.setCountry((String) object[9]);
        mapEvent.setPublisherId((Long) object[10]);
        mapEvent.setUrl((String) object[11]);
        mapEvent.setTrailIds((List<Long>) object[12]);
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

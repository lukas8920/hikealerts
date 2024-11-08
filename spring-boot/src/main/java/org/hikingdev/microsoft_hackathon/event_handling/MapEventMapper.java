package org.hikingdev.microsoft_hackathon.event_handling;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        mapEvent.setId((long) ((int) object[0]));
        mapEvent.setTitle((String) object[1]);
        mapEvent.setDescription((String) object[2]);
        mapEvent.setPublisher((String) object[3]);
        mapEvent.setStatus(mapStatus((String) object[4]));
        if (object[5] != null){
            mapEvent.setCreateDate(((Timestamp) object[5]).toLocalDateTime().format(formatter));
        }
        mapEvent.setLat((double) object[6]);
        mapEvent.setLng((double) object[7]);
        mapEvent.setEvent_id((String) object[8]);
        mapEvent.setCountry((String) object[9]);
        mapEvent.setPublisherId((long) ((int) object[10]));
        mapEvent.setUrl((String) object[11]);
        if (object[12] instanceof List<?>){
            mapEvent.setTrailIds((List<Long>) object[12]);
        } else if (object[12] instanceof Long){
            mapEvent.setTrailIds(List.of((Long) object[12]));
        }
        return mapEvent;
    }

    String mapStatus(String status){
        if (status.equals("Official")){
            return "Official";
        } else {
            return "Community";
        }
    }

    String mapStatus(Status status){
        if (Status.OFFICIAL.equals(status)){
            return "Official";
        } else {
            return "Community";
        }
    }
}

package org.hikingdev.microsoft_hackathon.event_handling;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Coordinate;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Status;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventResponseMapper {
    private static final Logger logger = LoggerFactory.getLogger(EventResponseMapper.class);

    private final WKBReader wkbReader = new WKBReader();

    public EventResponse map(Object[] object){
        EventResponse eventResponse = new EventResponse();
        if (object[0] instanceof Integer){
            long val = (int) object[0];
            eventResponse.setId(val);
        } else {
            eventResponse.setId((Long) object[0]);
        }
        eventResponse.setCountry((String) object[1]);
        eventResponse.setTrailname((String) object[2]);
        if (object[3] instanceof Integer){
            long val = (int) object[3];
            eventResponse.setTrail_id(val);
        } else {
            eventResponse.setTrail_id((Long) object[3]);
        }
        eventResponse.setTitle((String) object[4]);
        eventResponse.setDescription((String) object[5]);
        if (object[6] != null && object[6] instanceof Timestamp){
            eventResponse.setFromDate(((Timestamp) object[6]).toLocalDateTime());
        } else if (object[6] != null){
            eventResponse.setFromDate(((LocalDateTime) object[6]));
        }
        if (object[7] != null && object[7] instanceof Timestamp){
            eventResponse.setToDate(((Timestamp) object[7]).toLocalDateTime());
        } else if (object[7] != null){
            eventResponse.setToDate(((LocalDateTime) object[7]));
        }
        if (object[8] != null && object[8] instanceof Timestamp){
            eventResponse.setCrDate(((Timestamp) object[8]).toLocalDateTime());
        } else if (object[8] != null){
            eventResponse.setCrDate(((LocalDateTime) object[8]));
        }
        eventResponse.setPublisher((String) object[9]);
        if (object[4] instanceof Status){
            eventResponse.setStatus(mapStatus((Status) object[10]));
        } else {
            eventResponse.setStatus(mapStatus((String) object[10]));
        }
        eventResponse.setTrailMidPoint(new Coordinate((double) object[11], (double) object[12]));
        eventResponse.setCopyright((String) object[13]);
        eventResponse.setLicense((String) object[14]);
        if (object.length == 16){
            byte[] coordinates = (byte[]) object[15];
            try {
                Geometry geometry = wkbReader.read(coordinates);
                eventResponse.setCoordinates(map(geometry));
            } catch (ParseException e) {
                logger.error("Error while reading coordinates byte data.");
            }
        }
        return eventResponse;
    }

    List<Coordinate> map(Geometry geometry){
        List<Coordinate> outputCoordinates = new ArrayList<>();
        org.locationtech.jts.geom.Coordinate[] inputCoordinates = geometry.getCoordinates();
        for (org.locationtech.jts.geom.Coordinate c: inputCoordinates){
            Coordinate coordinate = new Coordinate(c.x, c.y);
            outputCoordinates.add(coordinate);
        }
        return outputCoordinates;
    }

    String mapStatus(Status status){
        if (status.equals(Status.COMMUNITY)){
            return "Community";
        } else {
            return "Official";
        }
    }

    String mapStatus(String status){
        if (status != null && status.equals("OFFICIAL")){
            return "Official";
        } else {
            return "Community";
        }
    }
}

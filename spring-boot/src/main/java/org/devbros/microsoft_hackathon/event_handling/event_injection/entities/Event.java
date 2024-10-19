package org.devbros.microsoft_hackathon.event_handling.event_injection.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "events")
public class Event implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Event.class.getName());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String event_id;
    private String region;
    private String country;
    @Column(name = "create_date_time")
    private LocalDateTime createDatetime;
    @Column(name = "from_date_time")
    private LocalDateTime fromDatetime;
    @Column(name = "to_date_time")
    private LocalDateTime toDatetime;
    private Long trailId;
    @JsonIgnore
    private String helperTrailName;
    private double midLongitudeCoordinate;
    private double midLatitudeCoordinate;
    private boolean displayMidCoordinate;
    private String title;
    private String description;
    private Long publisherId;
    private String url;

    public Event(Event event){
        this.id = event.getId();
        this.event_id = event.getEvent_id();
        this.region = event.getRegion();
        this.country = event.getCountry();
        this.createDatetime = event.getCreateDatetime();
        this.fromDatetime = event.getFromDatetime();
        this.toDatetime = event.getToDatetime();
        this.trailId = event.getTrailId();
        this.midLatitudeCoordinate = event.getMidLatitudeCoordinate();
        this.midLongitudeCoordinate = event.getMidLongitudeCoordinate();
        this.displayMidCoordinate = event.isDisplayMidCoordinate();
        this.helperTrailName = event.getHelperTrailName();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.url = event.getUrl();
        this.publisherId = event.getPublisherId();
    }

    public Event(RawEvent rawEvent, OpenAiEvent openAiEvent){
        this.title = rawEvent.getTitle();
        this.country = openAiEvent.getCountry();
        this.createDatetime = rawEvent.getCreateDateTime();
        this.region = openAiEvent.getParkName();
        if (openAiEvent.getParkName() == null){
            this.region = openAiEvent.getRegion();
        }
        this.event_id = openAiEvent.getEventId();
        this.description = rawEvent.getDescription();
        this.url = rawEvent.getUrl();
        this.publisherId = rawEvent.getPublisherId();
    }

    public void calculateMidCoordinate(Trail trail) throws ParseException {
        WKBReader wkbReader = new WKBReader();
        Geometry geometry = wkbReader.read(trail.getCoordinates());

        if (!(geometry instanceof Point)){
            LineString line = (LineString) geometry;
            // Use LengthIndexedLine to find points along the line based on its length
            LengthIndexedLine indexedLine = new LengthIndexedLine(line);
            // Get the total length of the line
            double totalLength = line.getLength();
            logger.debug("total length: " + totalLength);
            // Find the midpoint, which is at half the total length
            double midpointLength = totalLength / 2;
            logger.debug("midpoint length: " + midpointLength);
            // Get the coordinate at the midpoint length
            Coordinate midpoint = indexedLine.extractPoint(midpointLength);
            this.midLatitudeCoordinate = midpoint.y;
            logger.debug("Y: " + midpoint.y);
            this.midLongitudeCoordinate = midpoint.x;
            logger.debug("X: " + midpoint.x);
        } else {
            this.midLatitudeCoordinate = ((Point) geometry).getX();
            this.midLongitudeCoordinate = ((Point) geometry).getY();
        }
    };

    public void parseTimeInterval(String fromDatetime, String toDatetime) throws DateTimeParseException {
        fromDatetime = parseFromDateTimePlaceholders(fromDatetime);
        toDatetime = parseToDateTimePlaceholders(fromDatetime, toDatetime);

        this.fromDatetime = fromDatetime != null ? parseTimeString(fromDatetime) : null;
        this.toDatetime = toDatetime != null ? parseTimeString(toDatetime) : null;
    }

    private String parseFromDateTimePlaceholders(String fromDatetime){
        if (fromDatetime == null || fromDatetime.length() <= 4 ){
            return null;
        }

        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();

        int fromMonth = Integer.parseInt(fromDatetime.substring(3, 5));

        return fromMonth < month ? fromDatetime.replace("YYYY", String.valueOf(year + 1))
                : fromDatetime.replace("YYYY", String.valueOf(year));
    }

    private String parseToDateTimePlaceholders(String fromDatetime, String toDatetime){
        if (toDatetime == null || toDatetime.length() <= 4 ){
            return null;
        }

        int year = LocalDate.now().getYear();

        int fromMonth = Integer.parseInt(fromDatetime.substring(3, 5));
        int fromYear = Integer.parseInt(fromDatetime.substring(6, 10));
        int toMonth = Integer.parseInt(toDatetime.substring(3, 5));

        String toDatetimePlusOne = toDatetime.replace("YYYY", String.valueOf(year + 1));
        return toMonth < fromMonth ? toDatetimePlusOne
                : (fromYear == (year + 1) ? toDatetimePlusOne
                : toDatetime.replace("YYYY", String.valueOf(year)));
    }

    private LocalDateTime parseTimeString(String dateTime) throws DateTimeParseException {
        logger.debug("Trying to parse: " + dateTime);
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return LocalDateTime.parse(dateTime, formatter);
        } catch (DateTimeParseException e){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(dateTime, formatter);
            return localDate.atStartOfDay();
        }
    }
}

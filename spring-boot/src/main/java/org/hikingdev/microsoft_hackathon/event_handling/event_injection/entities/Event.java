package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
import java.util.ArrayList;
import java.util.List;

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
    @ElementCollection
    @CollectionTable(name = "events_trail_ids", foreignKey = @ForeignKey(
            name = "events_trail_ids_events_FK",
            foreignKeyDefinition = "foreign key (event_id) references events (id) on delete cascade"
    ))
    @Column(name = "trail_ids")
    private List<Long> trailIds;
    @Column(name = "mid_longitude_coordinate")
    private double midLongitudeCoordinate;
    @Column(name = "mid_latitude_coordinate")
    private double midLatitudeCoordinate;
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
        this.trailIds = event.getTrailIds();
        this.midLatitudeCoordinate = event.getMidLatitudeCoordinate();
        this.midLongitudeCoordinate = event.getMidLongitudeCoordinate();
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
        if (openAiEvent.getParkName() == null || openAiEvent.getParkName().isEmpty()){
            this.region = openAiEvent.getRegion();
        }
        this.event_id = openAiEvent.getEventId();
        this.description = rawEvent.getDescription();
        this.url = rawEvent.getUrl();
        this.publisherId = rawEvent.getPublisherId();
        this.toDatetime = rawEvent.getEndDateTime();
        this.fromDatetime = rawEvent.getStartDateTime();
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

    public void calculateMidCoordinate(List<byte[]> rawBytes) throws ParseException {
        WKBReader wkbReader = new WKBReader();
        List<LineString> lineStrings = new ArrayList<>();
        for (byte[] tmpBytes : rawBytes){
            LineString lineString = (LineString) wkbReader.read(tmpBytes);
            lineStrings.add(lineString);
        }

        double totalX = 0.0;
        double totalY = 0.0;
        int totalCoordinates = 0;

        for (LineString lineString : lineStrings) {
            Coordinate[] coordinates = lineString.getCoordinates();
            for (Coordinate coord : coordinates) {
                totalX += coord.getX();
                totalY += coord.getY();
                totalCoordinates++;
            }
        }

        // Avoid division by zero
        if (totalCoordinates == 0) {
            throw new IllegalArgumentException("No coordinates found in the provided LineStrings.");
        }

        // Calculate average X and Y values
        this.midLatitudeCoordinate = totalY / totalCoordinates;
        this.midLongitudeCoordinate = totalX / totalCoordinates;
    }

    public void parseTimeInterval(String fromDatetime, String toDatetime) {
        fromDatetime = fromDatetime != null ? parseFromDateTimePlaceholders(fromDatetime) : null;
        toDatetime = toDatetime != null ? parseToDateTimePlaceholders(fromDatetime, toDatetime) : null;

        LocalDateTime parsedFromDatetime = null;
        LocalDateTime parsedToDatetime = null;

        try {
            parsedFromDatetime = fromDatetime != null ? parseTimeString(fromDatetime): null;
        } catch (DateTimeParseException ignored){ }
        try {
            parsedToDatetime = toDatetime != null ? parseTimeString(toDatetime) : null;
        } catch (DateTimeParseException ignored){ }


        this.fromDatetime = fromDatetime != null && parsedFromDatetime != null ? parsedFromDatetime : this.fromDatetime;
        this.toDatetime = toDatetime != null && parsedToDatetime != null ? parsedToDatetime : this.toDatetime;
    }

    private String parseFromDateTimePlaceholders(String fromDatetime){
        if (fromDatetime == null || fromDatetime.length() <= 4 ){
            return null;
        }

        int day = LocalDate.now().getDayOfMonth();
        int month = LocalDate.now().getMonthValue();
        int year = LocalDate.now().getYear();


        try {
            int fromMonth = Integer.parseInt(fromDatetime.substring(3, 5));
            int fromDay = Integer.parseInt(fromDatetime.substring(0, 2));
            return fromMonth < month || (fromMonth == month && fromDay < day) ? fromDatetime.replace("YYYY", String.valueOf(year + 1))
                    : fromDatetime.replace("YYYY", String.valueOf(year));
        } catch (NumberFormatException e){
            logger.info("Non parseable characters in date string {}", fromDatetime.substring(3, 5));
            return null;
        }
    }

    private String parseToDateTimePlaceholders(String fromDatetime, String toDatetime){
        if (toDatetime == null || toDatetime.length() <= 4 ){
            return null;
        }
        if (fromDatetime == null && this.fromDatetime == null && !this.isValidDatetime(toDatetime)){
            return null;
        }
        if (fromDatetime == null && this.fromDatetime == null){
            return toDatetime;
        }

        int year = LocalDate.now().getYear();

        int fromDay = fromDatetime == null ? this.fromDatetime.getDayOfMonth() : Integer.parseInt(fromDatetime.substring(0, 2));
        int toDay = Integer.parseInt(toDatetime.substring(0, 2));
        int fromMonth = fromDatetime == null ? this.fromDatetime.getMonthValue() : Integer.parseInt(fromDatetime.substring(3, 5));
        int fromYear = fromDatetime == null ? this.fromDatetime.getYear() : Integer.parseInt(fromDatetime.substring(6, 10));
        int toMonth = Integer.parseInt(toDatetime.substring(3, 5));

        String toDatetimePlusOne = toDatetime.replace("YYYY", String.valueOf(year + 1));
        return toMonth < fromMonth || (toMonth == fromMonth && toDay < fromDay) ? toDatetimePlusOne
                : (fromYear == (year + 1) ? toDatetimePlusOne
                : toDatetime.replace("YYYY", String.valueOf(year)));
    }

    private boolean isValidDatetime(String toDatetime){
        try {
            parseTimeString(toDatetime);
        } catch (DateTimeParseException e){
            return false;
        }
        return true;
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

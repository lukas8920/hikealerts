package org.devbros.microsoft_hackathon.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.linearref.LengthIndexedLine;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
@Setter
@NoArgsConstructor
public class Event {
    private String event_id;
    private String region;
    private String country;
    private LocalDateTime createDatetime;
    private LocalDateTime fromDatetime;
    private LocalDateTime toDatetime;
    private Long trailId;
    private double midLongitudeCoordinate;
    private double midLatitudeCoordinate;
    private String title;
    private String description;
    private String publisher;
    private String url;

    //todo: add further properties once known
    public Event(Event event){
        this.event_id = event.getEvent_id();
        this.region = event.getRegion();
        this.country = event.getCountry();
        this.createDatetime = event.getCreateDatetime();
        this.fromDatetime = event.getFromDatetime();
        this.toDatetime = event.getToDatetime();
        this.trailId = event.getTrailId();
        this.midLatitudeCoordinate = event.getMidLatitudeCoordinate();
        this.midLongitudeCoordinate = event.getMidLongitudeCoordinate();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.url = event.getUrl();
        this.publisher = event.getPublisher();
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
        this.publisher = rawEvent.getPublisher();
    }

    public void calculateMidCoordinate(Trail trail){
        // Use LengthIndexedLine to find points along the line based on its length
        LengthIndexedLine indexedLine = new LengthIndexedLine(trail.getLine());
        // Get the total length of the line
        double totalLength = trail.getLine().getLength();
        // Find the midpoint, which is at half the total length
        double midpointLength = totalLength / 2;
        // Get the coordinate at the midpoint length
        Coordinate midpoint = indexedLine.extractPoint(midpointLength);
        this.midLatitudeCoordinate = midpoint.y;
        this.midLongitudeCoordinate = midpoint.x;
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

    private LocalDateTime parseTimeString(String dateTime){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return LocalDateTime.parse(dateTime, formatter);
    }
}

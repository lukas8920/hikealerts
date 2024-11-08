package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EventResponse {
    private Long id;
    private String country;
    private String trailname;
    private Long trail_id;
    private String title;
    private String description;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private LocalDateTime crDate;
    private String publisher;
    private String status;
    private Coordinate trailMidPoint;
}

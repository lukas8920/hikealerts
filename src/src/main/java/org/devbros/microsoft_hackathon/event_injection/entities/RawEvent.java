package org.devbros.microsoft_hackathon.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RawEvent {
    private Long id;
    private String eventId;
    private LocalDateTime createDateTime;
    private String country;
    private String title;
    private String unitCode;
    private String description;
    private String url;
    //todo needs to be added to the db
    private String publisher;
}

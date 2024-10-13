package org.devbros.microsoft_hackathon.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpenAiEvent {
    private String eventId;
    private String country;
    private String parkName;
    private String region;
    private String trailName;
    private String fromDate;
    private String toDate;
}

package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpenAiEvent {
    @JsonProperty("event_id")
    private String eventId;
    private String country;
    @JsonProperty("park_name")
    private String parkName;
    private String region;
    @JsonProperty("trail_name")
    private String trailName;
    @JsonProperty("from_date")
    private String fromDate;
    @JsonProperty("to_date")
    private String toDate;
    private String title;
    private String description;
}

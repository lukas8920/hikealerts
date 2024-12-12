package org.hikingdev.microsoft_hackathon.chat.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatEvent {
    private String type;
    @JsonProperty(value = "trail_name")
    private String trailName;
    private String country;
    private String title;
    private String description;
}

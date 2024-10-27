package org.devbros.microsoft_hackathon.event_handling.event_injection.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "raw_events")
public class RawEvent {
    @Id
    private Long id;
    @OpenAiInput
    private String eventId;
    @Column(name = "create_date")
    private LocalDateTime createDateTime;
    @OpenAiInput
    private String country;
    @OpenAiInput
    private String title;
    @OpenAiInput
    @Column(name = "park_code")
    private String unitCode;
    @OpenAiInput
    private String description;
    private String url;
    private Long publisherId;
    private String parkRegionName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
}

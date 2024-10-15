package org.devbros.microsoft_hackathon.event_injection.entities;

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
    private String eventId;
    @Column(name = "create_date")
    private LocalDateTime createDateTime;
    private String country;
    private String title;
    @Column(name = "park_code")
    private String unitCode;
    private String description;
    private String url;
    private Long publisherId;
}

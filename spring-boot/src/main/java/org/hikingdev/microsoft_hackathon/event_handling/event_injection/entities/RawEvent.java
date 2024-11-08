package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public RawEvent(String country, String title, String description, LocalDate fromDate, LocalDate toDate){
        this.eventId = "com_" + UUID.randomUUID().toString();
        this.country = country;
        this.title = title;
        this.description = description;
        if (fromDate != null){
            this.startDateTime = fromDate.atStartOfDay();
        }
        if (endDateTime != null){
            this.endDateTime = toDate.atTime(LocalTime.MAX);
        }
        // set publisherId to community publisher by default
        this.publisherId = 3L;
    }

    public String parseToJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // Create the message structure
        Map<String, Object> messageDict = new HashMap<>();
        messageDict.put("country", country);

        List<Map<String, String>> alerts = List.of(
                Map.of("event_id", eventId, "title", title, "description", description)
        );

        messageDict.put("alerts", alerts);

        return mapper.writeValueAsString(messageDict);
    }
}

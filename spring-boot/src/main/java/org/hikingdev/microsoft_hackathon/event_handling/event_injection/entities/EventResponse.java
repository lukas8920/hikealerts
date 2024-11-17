package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EventResponse {
    @Schema(description = "ID of the event in the hiking-alerts.org database (required for deletion).")
    private Long id;
    @Schema(description = "ISO 3166-1 alpha-2 country code", example = "NZ")
    private String country;
    @Schema(description = "Name of the trail affected by the event", example = "Mount Elvis Track")
    private String trailname;
    @Schema(description = "ID of the trail in the hiking-alerts.org database.")
    private Long trail_id;
    @Schema(description = "Title of the event")
    private String title;
    @Schema(description = "Description for the event")
    private String description;
    @Schema(description = "From date which specifies when the event becomes valid. The date is provided in format yyyy-MM-dd HH:MM:SS.", nullable = true, example = "2024-12-05 12:50:15")
    private LocalDateTime fromDate;
    @Schema(description = "To date which specifies when the event becomes invalid. The date is provided in format yyyy-MM-dd HH:MM:SS.", nullable = true, example = "2024-12-08 13:50:15")
    private LocalDateTime toDate;
    @Schema(description = "Creation timestamp for the event in format yyyy-MM-dd HH:MM:SS", example = "2024-11-09 12:00:00")
    private LocalDateTime crDate;
    @Schema(description = "Name of the institution which published the event. Events published by the community have the tag '-'.", example = "US National Park Service")
    private String publisher;
    @Schema(description = "Tag defining who published the event.", allowableValues = {"Community", "Official"})
    private String status;
    @Schema(description = "Geographic coordinates for the mid point of the trail associated with the event, where x is the longitude coordinate and y is the latitude coordinate.")
    private Coordinate trailMidPoint;
    @Schema(description = "Copyright credit required by the data provider for the event.")
    private String copyright;
    @Schema(description = "License applied by the data provider for the event data.")
    private String license;
}

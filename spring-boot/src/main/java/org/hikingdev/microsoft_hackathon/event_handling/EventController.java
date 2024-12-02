package org.hikingdev.microsoft_hackathon.event_handling;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.EventResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.user.entities.MessageResponse;
import org.hikingdev.microsoft_hackathon.util.AiException;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/events")
public class EventController {
    private static final Logger logger = LoggerFactory.getLogger(EventController.class.getName());

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @CrossOrigin
    @GetMapping("/pull")
    @Hidden
    public ResponseEntity<List<MapEvent>> getEventData(@RequestParam int offset, @RequestParam int limit) throws BadRequestException {
        logger.debug("Request event data - offset: " + offset + " - limit: " + limit);
        List<MapEvent> mapEvents = this.eventService.pullData(offset, limit);
        return ResponseEntity.ok(mapEvents);
    }

    @Operation(summary = "Get a list of events.", description = "Retrieves a list of events which hiking-alerts.org was able to map to geospatial data.", operationId = "queryEvents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The queried events.",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = EventResponse.class))) }),
            @ApiResponse(responseCode = "400", description = "Invalid user input. The provided parameters are not meeting the requirements. See response message for details.",
                    content = @Content)
    })
    @CrossOrigin
    @GetMapping("/query")
    public ResponseEntity<List<EventResponse>> queryEvents(@Parameter(description = "Bounding box to query events where the mid point of the trail lies within the box. The boundary box is defined by the minimum longitude, the minimum latitude, the maximum longitude and the maximum latitude.", example = "1.14,1.14,4.8,4.8", required = false) @RequestParam(name = "boundary", required = false) String boundary,
                                                           @Parameter(description = "Country in ISO 3166-1 alpha-2 format. Valid countries are CH, IE, NZ & US. Either a boundary box or a country is required for the query.", example = "NZ", required = false) @RequestParam(name = "country", required = false) String country,
                                                           @Parameter(description = "From date which specifies when the event becomes valid (might be not given by the event provider). The date should be in format yyyy-MM-dd.", example = "2024-11-05", required = false) @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                           @Parameter(description = "To date which specifies when the event becomes invalid (might be not given by the event provider). The date should be in format yyyy-MM-dd", example = "2024-12-05", required = false) @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                           @Parameter(description = "Create date which specifies when the event was provided by data provider. Returns all events greater or equal to the create date. The date should be in format yyyy-MM-dd", example = "2024-11-09", required = false) @RequestParam(name = "createDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createDate,
                                                           @Parameter(description = "Boolean flag indicating whether events with null values for the from or the to date should be returned. The default value is true.") @RequestParam(name = "nullDates", defaultValue = "true", required = false) boolean nullDates,
                                                           @Parameter(description = "Filters on who created the events. Valid values are: 'All', 'Community' or 'Official'. Default value is 'All'.", example = "Official", required = false) @RequestParam(name = "createdBy", required = false, defaultValue = "All") String createdBy,
                                                           @Parameter(description = "The offset for the results in the event datasource. Enables paging through the events. Default value is 0", example = "50", required = false) @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
                                                           @Parameter(description = "The limit defining the maximum number of results to return. Default value is 50. Maximum value is 100.", example = "40", required = false) @RequestParam(name = "limit", required = false, defaultValue = "50") int limit) throws BadRequestException {
        List<EventResponse> events = this.eventService.requestEvents(boundary, country, fromDate, toDate, createDate,
                nullDates, createdBy, offset, limit);
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "Publish your own event.", description = "Allows to publish an event and requests hiking-alerts.org to match the event with geospatial data. By default events are published via the 'Community' tag. Anybody who belongs to the community can adjust/delete community events. On request via mail you can become member of an official institution. The events are then published via this institution. And only members of the institution can adjust or delete the event.", operationId = "queryEvents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback whether event injection worked.",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))) }),
            @ApiResponse(responseCode = "400", description = "Invalid user input. The provided parameters are not meeting the requirements. See response message for details.",
                    content = @Content)
    })
    @CrossOrigin
    @PostMapping("/publish")
    public ResponseEntity<MessageResponse> publishEvent(@Parameter(description = "A brief title for the event which may not exceed 200 words. In order to allow geospatial mapping either the description or the title should mention the trail name.", example = "Track closure", required = true) @RequestParam(name = "title", required = true) String title,
                                                        @Parameter(description = "A description of the event which may not exceed 2000 words. In order to allow geospatial mapping either the description or the title should mention the trail name.", example = "Mount Herbert Walkway track closed due to lumbering operations.", required = true) @RequestParam(name = "description", required = true) String description,
                                                        @Parameter(description = "Country in ISO 3166-1 alpha-2 format. Valid countries are IE, NZ & US.", example = "NZ", required = true) @RequestParam(name = "country", required = true) String country,
                                                        @Parameter(description = "From date which specifies when the event becomes valid (might be not given by the event provider). The date should be in format yyyy-MM-dd.", example = "2024-11-05", required = false) @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                        @Parameter(description = "To date which specifies when the event becomes invalid (might be not given by the event provider). The date should be in format yyyy-MM-dd", example = "2024-12-05", required = false) @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) throws BadRequestException, AiException {
        MessageResponse messageResponse = this.eventService.publishEvent(country, title, description, fromDate, toDate);
        return ResponseEntity.ok(messageResponse);
    }

    @Operation(summary = "Delete published events", description = "Delete an event from the hiking-alerts.org database", operationId = "queryEvents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feedback whether event deletion worked.",
                    content = { @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class))) })
    })
    @CrossOrigin
    @PostMapping("/delete")
    public ResponseEntity<MessageResponse> deleteEvent(@Parameter(description = "The event of the id. Id's can be retrieved via the query endpoint.", example = "1", required = true) @RequestParam(name = "id", required = true) String id) throws BadRequestException {
        MessageResponse messageResponse = this.eventService.deleteEvent(id);
        return ResponseEntity.ok(messageResponse);
    }
}

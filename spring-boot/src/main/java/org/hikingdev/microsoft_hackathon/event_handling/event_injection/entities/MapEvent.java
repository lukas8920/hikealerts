package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MapEvent implements Serializable {
    private Long id;
    private String title;
    private String description;
    private String publisher;
    private String status;
    @JsonProperty(value = "create_date")
    private String createDate;
    private double lat;
    private double lng;
    private String url;
    @JsonProperty(value = "trail_ids")
    private List<Long> trailIds;
    @JsonIgnore
    private String event_id;
    @JsonIgnore
    private String country;
    @JsonIgnore
    private Long publisherId;
    private String copyright;
    private String license;

    @Override
    public String toString(){
        String builder = id +
                "," +
                title +
                "," +
                description +
                "," +
                status +
                "," +
                createDate +
                "," +
                lat +
                "," +
                lng +
                "," +
                trailIds +
                "," +
                event_id +
                "," +
                country +
                "," +
                publisherId +
                "," +
                copyright +
                "," +
                license;
        return builder;
    }
}

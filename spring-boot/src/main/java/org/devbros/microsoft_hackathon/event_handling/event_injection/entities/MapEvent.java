package org.devbros.microsoft_hackathon.event_handling.event_injection.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "events")
public class MapEvent implements Serializable {
    @Id
    private Long id;
    private String title;
    private String description;
    private String publisher;
    private String status;
    @JsonProperty(value = "create_date")
    @Column(name = "create_date_time")
    private String createDate;
    @Column(name = "mid_latitude_coordinate")
    private double lat;
    @Column(name = "mid_longitude_coordinate")
    private double lng;
    private String url;
    @JsonIgnore
    private String event_id;
    @JsonIgnore
    private String country;
    @JsonIgnore
    private Long publisherId;
}

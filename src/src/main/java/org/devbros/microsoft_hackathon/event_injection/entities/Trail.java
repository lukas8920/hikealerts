package org.devbros.microsoft_hackathon.event_injection.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "geodata_trails", schema = "dbo")
public class Trail {
    @Id
    private Long id;
    private Long trailId;
    private String trailname;
    private String country;
    private String maplabel;
    private String unitcode;
    private String unitname;
    private String regioncode;
    private String maintainer;
    private byte[] coordinates;
}

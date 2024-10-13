package org.devbros.microsoft_hackathon.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@NoArgsConstructor
public class Trail {
    private Long id;
    private Long trailId;
    private String trailName;
    private String country;
    private String maplabel;
    private String unitcode;
    private String unitname;
    private String regioncode;
    private String maintainer;
    private LineString line;
}

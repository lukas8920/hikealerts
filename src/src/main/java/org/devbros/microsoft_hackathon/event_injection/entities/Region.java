package org.devbros.microsoft_hackathon.event_injection.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
@NoArgsConstructor
public class Region {
    private Long id;
    private String regionId;
    private String country;
    private String code;
    private String name;
    private Polygon polygon;
}

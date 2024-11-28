package org.hikingdev.microsoft_hackathon.map_layer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

@Getter
@Setter
@AllArgsConstructor
public class SpatialItem {
    private Long id;
    private String trailname;
    private Geometry lineString;
}

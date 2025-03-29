package org.hikingdev.microsoft_hackathon.geotrek.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hikingdev.microsoft_hackathon.util.json.LineStringDeserializer;
import org.hikingdev.microsoft_hackathon.util.json.LineStringSerializer;
import org.locationtech.jts.geom.LineString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeotrekTrail {
    private String id;
    private String name;
    private String maintainer;
    @JsonDeserialize(using = LineStringDeserializer.class)
    @JsonSerialize(using = LineStringSerializer.class)
    private LineString coordinates;

    @Override
    public String toString(){
        return "Geotrek Trail - id: " + id + " - name: " + name + " - maintainer: " + maintainer;
    }
}

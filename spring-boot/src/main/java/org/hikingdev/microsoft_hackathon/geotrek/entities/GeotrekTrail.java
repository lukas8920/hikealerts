package org.hikingdev.microsoft_hackathon.geotrek.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hikingdev.microsoft_hackathon.util.geodata.Math;
import org.hikingdev.microsoft_hackathon.util.json.LineStringDeserializer;
import org.hikingdev.microsoft_hackathon.util.json.LineStringSerializer;
import org.locationtech.jts.geom.LineString;

import java.util.List;

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
    private Integer source;
    private Integer target;
    private String country;

    @Override
    public String toString(){
        return "Geotrek Trail - id: " + id + " - name: " + name + " - maintainer: " + maintainer;
    }

    public GeotrekTrail(GeotrekTrail geotrekTrail, LineString lineString){
        this.id = geotrekTrail.id;
        this.name = geotrekTrail.name;
        this.maintainer = geotrekTrail.maintainer;
        this.coordinates = lineString;
    }

    public static GeotrekTrail joinGeotrekTrails(List<GeotrekTrail> geotrekTrails){
        GeotrekTrail outputTrail = getFirstTrail(geotrekTrails);
        int firstTrailTarget = outputTrail.getTarget();

        GeotrekTrail nextTrail = getNextGeotrail(outputTrail, geotrekTrails, firstTrailTarget);
        while (nextTrail != null){
            LineString joinedLineString = Math.joinLineStrings(outputTrail.getCoordinates(), nextTrail.getCoordinates());
            outputTrail = new GeotrekTrail(outputTrail, joinedLineString);
            nextTrail = getNextGeotrail(nextTrail, geotrekTrails, firstTrailTarget);
        }
        return outputTrail;
    }

    private static GeotrekTrail getFirstTrail(List<GeotrekTrail> geotrekTrails){
        GeotrekTrail sourceGeotrail = geotrekTrails.get(0);
        GeotrekTrail outputGeotrail = null;

        int originalSource = sourceGeotrail.getSource();
        int tmpSource = originalSource;

        while (outputGeotrail == null){
            int currentSource = tmpSource;

            for (GeotrekTrail targetGeotrail: geotrekTrails){
                if (!sourceGeotrail.equals(targetGeotrail) && sourceGeotrail.getSource().equals(targetGeotrail.getTarget())){
                    sourceGeotrail = targetGeotrail;
                    currentSource = targetGeotrail.getSource();
                    break;
                }
            }

            if (currentSource == tmpSource || currentSource == originalSource){
                outputGeotrail = sourceGeotrail;
            }
            if (outputGeotrail != null){
                tmpSource = outputGeotrail.getSource();
            }
        }
        return outputGeotrail;
    }

    private static GeotrekTrail getNextGeotrail(GeotrekTrail geotrekTrail, List<GeotrekTrail> geotrekTrails, int firstTrail){
        GeotrekTrail outputTrail = null;
        for (GeotrekTrail targetGeotrail: geotrekTrails){
            if (!geotrekTrail.equals(targetGeotrail) && geotrekTrail.getTarget().equals(targetGeotrail.getSource())){
                outputTrail = targetGeotrail.getTarget() == firstTrail ? null : targetGeotrail;
                break;
            }
        }
        return outputTrail;
    }
}

package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hikingdev.microsoft_hackathon.util.geodata.Math;
import org.locationtech.jts.io.ParseException;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "geodata_trails")
public class Trail extends MatchProvider {
    @Id
    private Long id;
    private String trailId;
    private String trailname;
    private String country;
    private String maplabel;
    private String unitcode;
    private String unitname;
    private String regioncode;
    private String maintainer;
    private byte[] coordinates;

    public String[] getCandidateStrings(){
        return new String[]{trailname, maplabel};
    }

    public String convertBytesToString() throws ParseException {
        return Math.convertByteArrayToString(this.coordinates);
    }
}

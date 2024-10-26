package org.devbros.microsoft_hackathon.event_handling.event_injection.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "geodata_trails", schema = "dbo")
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
}

package org.devbros.microsoft_hackathon.event_handling.event_injection.entities;

import jakarta.persistence.Column;
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
@Table(name = "geodata_regions")
public class Region extends MatchProvider {
    @Id
    private Long id;
    private String regionId;
    private String country;
    private String code;
    private String name;
    @Column(name = "boundaries")
    private byte[] polygon;

    @Override
    public String[] getCandidateStrings() {
        return new String[]{name};
    }
}

package org.hikingdev.microsoft_hackathon.geotrek.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GeonamesResponse {
    private String languages;
    private String distance;
    private String countryCode;
    private String countryName;
}

package org.devbros.microsoft_hackathon.repository.regions;

import org.devbros.microsoft_hackathon.event_injection.entities.Region;

import java.util.List;

public interface IRegionRepository {
    List<Region> findRegionByRegionNameAndCountry(String regionName, String country);
}

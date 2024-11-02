package org.hikingdev.microsoft_hackathon.repository.regions;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Region;

import java.util.List;

public interface IRegionRepository {
    List<Region> findRegionByRegionNameAndCountry(String regionName, String country);
    List<Region> findUniqueRegionName(String regionName, String country);
}

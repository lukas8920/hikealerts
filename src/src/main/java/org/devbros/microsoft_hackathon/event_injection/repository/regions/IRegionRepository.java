package org.devbros.microsoft_hackathon.event_injection.repository.regions;

import org.devbros.microsoft_hackathon.event_injection.entities.Region;

public interface IRegionRepository {
    Region findRegionByRegionName(String regionName);
}

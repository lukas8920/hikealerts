package org.hikingdev.microsoft_hackathon.repository.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;

public interface IGeotrekRepository {
    void save(GeotrekToken geotrekToken);
    GeotrekToken find(Long userId);
}

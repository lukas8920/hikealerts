package org.hikingdev.microsoft_hackathon.repository.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GeotrekRepository implements IGeotrekRepository {
    private final IGeotrekJpaRepository iGeotrekJpaRepository;

    @Autowired
    public GeotrekRepository(IGeotrekJpaRepository iGeotrekJpaRepository){
        this.iGeotrekJpaRepository = iGeotrekJpaRepository;
    }

    @Override
    public void save(GeotrekToken geotrekToken) {
        this.iGeotrekJpaRepository.save(geotrekToken);
    }

    @Override
    public GeotrekToken find(Long userId) {
        Optional<GeotrekToken> optToken = this.iGeotrekJpaRepository.findById(userId);
        return optToken.orElse(null);
    }
}

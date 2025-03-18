package org.hikingdev.microsoft_hackathon.repository.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IGeotrekJpaRepository extends JpaRepository<GeotrekToken, Long> {
    Optional<GeotrekToken> findFirstByUserId(Long userId);
}

package org.hikingdev.microsoft_hackathon.repository.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IGeotrekJpaRepository extends JpaRepository<GeotrekToken, Long> {
}

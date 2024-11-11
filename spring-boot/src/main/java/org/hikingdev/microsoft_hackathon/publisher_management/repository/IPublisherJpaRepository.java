package org.hikingdev.microsoft_hackathon.publisher_management.repository;

import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPublisherJpaRepository extends JpaRepository<Publisher, Long> {
    Publisher findUserById(Long id);
}

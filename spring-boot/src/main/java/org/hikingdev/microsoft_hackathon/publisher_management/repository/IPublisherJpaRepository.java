package org.hikingdev.microsoft_hackathon.publisher_management.repository;

import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IPublisherJpaRepository extends JpaRepository<Publisher, Long> {
    Publisher findUserById(Long id);
    @Query(value = "SELECT p.* FROM publisher p INNER JOIN users u ON u.publisher_id = p.id WHERE u.id = :user_id", nativeQuery = true)
    Publisher findPublisherByUserId(@Param("user_id") Long userId);
}

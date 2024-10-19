package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IEventJpaRepository extends JpaRepository<Event, Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM events WHERE event_id = :id AND country = :country", nativeQuery = true)
    void deleteByIdAndCountry(@Param("id") String event_id, @Param("country") String country);

    @Query(value = "SELECT * FROM events WHERE event_id IN (:ids) AND country = :country", nativeQuery = true)
    List<Event> findIdByEventIdAndCountry(@Param("ids") String ids, @Param("country") String country);
}

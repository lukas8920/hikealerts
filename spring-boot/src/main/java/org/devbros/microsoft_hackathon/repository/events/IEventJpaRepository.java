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

    @Query(value = "SELECT e.id, e.title, e.description, p.name, p.status, e.create_date_time, e.mid_latitude_coordinate, e.mid_longitude_coordinate, e.event_id, e.country, e.publisher_id, e.url, i.trail_ids FROM events e JOIN publisher p ON p.id = e.publisher_id JOIN events_trail_ids i ON i.event_id = e.id WHERE e.event_id IN (:ids) AND e.country = :country", nativeQuery = true)
    List<Object[]> findByEventIdsAndCountry(@Param("ids") List<String> ids, @Param("country") String country);

    @Query(value = "SELECT e.id, e.title, e.description, p.name, p.status, e.create_date_time, e.mid_latitude_coordinate, e.mid_longitude_coordinate, e.event_id, e.country, e.publisher_id, e.url, i.trail_ids FROM events e JOIN publisher p ON p.id = e.publisher_id JOIN events_trail_ids i ON i.event_id = e.id WHERE e.event_id = :id AND e.country = :country", nativeQuery = true)
    List<Object[]> findByEventIdAndCountry(@Param("id") String id, @Param("country") String country);
}

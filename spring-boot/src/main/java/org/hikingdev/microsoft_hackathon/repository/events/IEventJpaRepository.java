package org.hikingdev.microsoft_hackathon.repository.events;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IEventJpaRepository extends JpaRepository<Event, Long> {
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM events WHERE event_id = :id AND country = :country", nativeQuery = true)
    void deleteByIdAndCountry(@Param("id") String event_id, @Param("country") String country);

    @Query(value = "SELECT e.id, e.title, e.description, p.name, p.status, e.create_date_time, e.mid_latitude_coordinate, e.mid_longitude_coordinate, e.event_id, e.country, e.publisher_id, e.url, i.trail_ids, p.copyright, p.license FROM events e JOIN publisher p ON p.id = e.publisher_id JOIN events_trail_ids i ON i.event_id = e.id WHERE e.event_id IN (:ids) AND e.country = :country", nativeQuery = true)
    List<Object[]> findByEventIdsAndCountry(@Param("ids") List<String> ids, @Param("country") String country);

    @Query(value = "SELECT e.id, e.title, e.description, p.name, p.status, e.create_date_time, e.mid_latitude_coordinate, e.mid_longitude_coordinate, e.event_id, e.country, e.publisher_id, e.url, i.trail_ids, p.copyright, p.license FROM events e JOIN publisher p ON p.id = e.publisher_id JOIN events_trail_ids i ON i.event_id = e.id WHERE e.event_id = :id AND e.country = :country", nativeQuery = true)
    List<Object[]> findByEventIdAndCountry(@Param("id") String id, @Param("country") String country);

    @Query(value = "SELECT e.id, e.title, e.description, p.name, p.status, e.create_date_time, e.mid_latitude_coordinate, e.mid_longitude_coordinate, e.event_id, e.country, e.publisher_id, e.url, i.trail_ids, p.copyright, p.license FROM events e JOIN publisher p ON p.id = e.publisher_id JOIN events_trail_ids i ON i.event_id = e.id WHERE e.id = :id AND p.id = :publisher", nativeQuery = true)
    List<Object[]> findByIdAndPublisher(@Param("id") Long id, @Param("publisher") Long publisher);

    @Query(value = "SELECT e.id, e.title, e.description, p.name, p.status, e.create_date_time, e.mid_latitude_coordinate, e.mid_longitude_coordinate, e.event_id, e.country, e.publisher_id, e.url, i.trail_ids, p.copyright, p.license \n" +
            "FROM events e \n" +
            "JOIN publisher p ON p.id = e.publisher_id \n" +
            "JOIN events_trail_ids i ON i.event_id = e.id \n" +
            "JOIN geodata_trails t ON t.id  = i.trail_ids \n" +
            "WHERE (dbo.Levenshtein(t.trailname, :trail, 4) <= 4 or dbo.Levenshtein(t.maplabel, :trail, 4) <= 4) AND e.country = :country", nativeQuery = true)
    List<Object[]> findEventsByTrailAndCountry(@Param("trail") String trail, @Param("country") String country);

    @Query(value = "EXEC InsertEvents @event_id = :eventId, @region = :region, @country = :country, @create_date_time = :createDateTime, " +
            "@from_date_time = :fromDateTime, @to_date_time = :toDateTime, @mid_longitude_coordinate = :midLongitudeCoordinate, " +
            "@mid_latitude_coordinate = :midLatitudeCoordinate, @title = :title, @description = :description, @publisher_id = :publisherId, " +
            "@url = :url, @override_data = :overrideData", nativeQuery = true)
    Event saveEvent(String eventId, String region, String country, LocalDateTime createDateTime, LocalDateTime fromDateTime, LocalDateTime toDateTime,
                          double midLongitudeCoordinate, double midLatitudeCoordinate, String title, String description, Long publisherId, String url,
                          boolean overrideData);
}

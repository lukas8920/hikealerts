package org.devbros.microsoft_hackathon.repository.events;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Event;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
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

    @Query(value = "SELECT id, title, description, create_date_time, mid_latitude_coordinate, mid_longitude_coordinate, url, event_id, country FROM events WHERE event_id IN (:ids) AND country = :country", nativeQuery = true)
    List<MapEvent> findIdByEventIdAndCountry(@Param("ids") String ids, @Param("country") String country);

    @Modifying
    @Transactional
    @Query(value = "EXECUTE InsertEvents @event_id = :event_id, @region = :region, @country = :country, @create_date_time = :create_date_time, @from_date_time = :from_date_time, @to_date_time = :to_date_time, @trail_id = :trail_id, @mid_longitude_coordinate = :mid_longitude_coordinate, @mid_latitude_coordinate = :mid_latitude_coordinate, @display_mid_coordinate = :display_mid_coordinate, @title = :title, @description = :description, @publisher_id = :publisher_id, @url = :url, @helper_trail_name = :helper_trail_name", nativeQuery = true)
    void saveEvent (@Param("event_id") String eventId, @Param("region") String region, @Param("country") String country, @Param("create_date_time") LocalDateTime createDateTime, @Param("from_date_time") LocalDateTime fromDateTime,
                    @Param("to_date_time") LocalDateTime toDateTime, @Param("trail_id") Long trailId, @Param("helper_trail_name") String helperTrailName, @Param("mid_longitude_coordinate") double midLongitudeCoordinate,
                    @Param("mid_latitude_coordinate") double mid_latitude_coordinate, @Param("display_mid_coordinate") boolean displayMidCoordinate, @Param("title") String title,
                    @Param("description") String description, @Param("publisher_id") Long publisherId, @Param("url") String url);
}

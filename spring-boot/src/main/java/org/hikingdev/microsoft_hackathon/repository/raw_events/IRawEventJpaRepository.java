package org.hikingdev.microsoft_hackathon.repository.raw_events;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IRawEventJpaRepository extends JpaRepository<RawEvent, Long> {
    RawEvent findFirstByEventIdAndCountry(String eventId, String country);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM raw_events WHERE event_id = :id AND country = :country", nativeQuery = true)
    void deleteByIdAndCountry(@Param("id") String event_id, @Param("country") String country);
}

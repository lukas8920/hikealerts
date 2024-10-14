package org.devbros.microsoft_hackathon.repository.trails;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ITrailJpaRepository extends JpaRepository<Trail, Long> {
    //for test purposes only
    @Modifying
    @Transactional
    @Query(value = "EXEC TestOnlyInsertGeodataTrails @trail_id = :trail_id, @country = :country, @trailname = 'dummy', @unitcode = :unitcode, @unitname = 'dummy', @lineString = :coordinates", nativeQuery = true)
    void saveTrail (@Param("trail_id") Long trail_id, @Param("country") String country, @Param("unitcode") String unitcode, @Param("coordinates") byte[] geometry);

    //for test purposes only
    @Modifying
    @Transactional
    void deleteAllByTrailIdAndCountry(Long trail_id, String country);

    @Query(value = "SELECT id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE unitcode = :unitcode AND country = :country", nativeQuery = true)
    Slice<Trail> findAllByUnitcodeAndCountry(@Param("unitcode") String unitCode,@Param("country") String country, Pageable pageable);

    @Query(value = "SELECT id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE country = :country", nativeQuery = true)
    Slice<Trail> findAllByCountry(@Param("country") String country, Pageable pageable);
}
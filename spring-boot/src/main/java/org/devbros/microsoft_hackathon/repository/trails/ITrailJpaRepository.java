package org.devbros.microsoft_hackathon.repository.trails;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Query(value = "SELECT TOP 100 id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE unitcode = :unitcode AND country = :country AND id > :offset " +
            "ORDER BY id", nativeQuery = true)
    List<Trail> findAllByUnitcodeAndCountry(@Param("unitcode") String unitCode,@Param("country") String country, @Param("offset") Long offset);

    @Query(value = "SELECT TOP 1000 id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "where country = :country AND id > :offset " +
            "ORDER BY id", nativeQuery = true)
    List<Trail> findAllByCountry(@Param("country") String country, @Param("offset") Long offset);

    @Query(value = "SELECT TOP 100 id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE unitcode = :unitcode AND country = :country", nativeQuery = true)
    List<Trail> findAllByCountryAndUnitcode(String country, String unitcode);
}
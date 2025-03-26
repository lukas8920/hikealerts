package org.hikingdev.microsoft_hackathon.repository.trails;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ITrailJpaRepository extends JpaRepository<Trail, Long> {
    // for test purposes only
    @Modifying
    @Transactional
    @Query(value = "EXEC TestOnlyInsertGeodataTrails @trail_id = :trail_id, @country = :country, @trailname = 'dummy', @unitcode = :unitcode, @unitname = 'dummy', @lineString = :coordinates", nativeQuery = true)
    void saveTrail(@Param("trail_id") String trail_id, @Param("country") String country, @Param("unitcode") String unitcode, @Param("coordinates") String geometry);

    // for test purposes only
    @Modifying
    @Transactional
    void deleteAllByTrailIdAndCountry(String trail_id, String country);

    @Query(value = "SELECT id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE trail_id = :trailId AND country = :country", nativeQuery = true)
    Trail findByTrailIdAndCountry(String trailId, String country);

    @Query(value = "SELECT TOP 100 id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE unitcode = :unitcode AND country = :country AND id > :offset " +
            "ORDER BY id", nativeQuery = true)
    List<Trail> findAllByUnitcodeAndCountry(@Param("unitcode") String unitCode, @Param("country") String country, @Param("offset") Long offset);

    @Query(value = "SELECT TOP 100 id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "where country = :country AND id > :offset " +
            "ORDER BY id", nativeQuery = true)
    List<Trail> findAllByCountry(@Param("country") String country, @Param("offset") Long offset);

    @Query(value = "SELECT id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE unitcode = :unitcode AND country = :country", nativeQuery = true)
    List<Trail> findAllByCountryAndUnitcode(String country, String unitcode);

    List<Trail> findAllByCountry(String country);

    @Query(value = "SELECT id, trail_id, trailname, country, maplabel, unitcode, unitname, regioncode, maintainer, coordinates.STAsBinary() AS coordinates " +
            "FROM geodata_trails " +
            "WHERE id IN (:ids)", nativeQuery = true)
    List<Trail> findAllByIds(List<Long> ids);

    @Query(value = "EXEC GetTrailsAfterOffset @offset = :offset, @limit = :limit", nativeQuery = true)
    List<Trail> getTrailsAfterOffset(int offset, int limit);

    @Query(value = "EXEC GetTrailsByEventIdAndCountry @event_id = :event_id, @country = :country", nativeQuery = true)
    List<Trail> findTrailsByEventIdAndCountry(String event_id, String country);

    @Query(value = "EXEC InsertGeodataTrails @trail_id = :trail_id, @country = :country, @trailname = :trailname, @maintainer = :maintainer, @lineString = :lineString", nativeQuery = true)
    void save(@Param("trail_id") String trail_id, @Param("country") String country, @Param("trailname") String trailname, @Param("maintainer") String maintainer, @Param("lineString") String lineString);

    @Query(value = "DELETE FROM geodata_trails WHERE trail_id = :trail_id AND maintainer in (:publishers)", nativeQuery = true)
    void delete(@Param("trail_id") String trail_id, @Param("publishers") List<String> publishers);
}
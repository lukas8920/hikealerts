package org.hikingdev.microsoft_hackathon.repository.regions;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface IRegionJpaRepository extends JpaRepository<Region, Long> {
    //for test purposes only
    @Modifying
    @Transactional
    @Query(value = "EXEC TestOnlyInsertGeodataRegions @region_id = :region_id, @country = :country, @code = :code, @name = :name, @boundaries = :boundaries", nativeQuery = true)
    void saveRegion (@Param("region_id") String region_id, @Param("country") String country, @Param("code") String code, @Param("name") String name, @Param("boundaries") byte[] boundaries);

    //for test purposes only
    @Modifying
    @Transactional
    void deleteAllByRegionIdAndCountry(String region_id, String country);

    @Query(value = "SELECT TOP 200 id, region_id, country, code, name, boundaries.STAsBinary() AS boundaries " +
            "FROM geodata_regions " +
            "WHERE country = :country AND id > :offset " +
            "ORDER BY id", nativeQuery = true)
    List<Region> findRegionsByCountry(@Param("country") String country, @Param("offset") Long offset);

    @Query(value = "SELECT id, region_id, country, code, name, boundaries.STAsBinary() AS boundaries \n" +
            "FROM geodata_regions\n" +
            "WHERE country = :country AND name = :name", nativeQuery = true)
    List<Region> findAllByCountryAndName(@Param("country") String country, @Param("name") String name);
}

package org.devbros.microsoft_hackathon.repository.regions;

import org.devbros.microsoft_hackathon.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class RegionRepositoryTest {
    @Autowired
    private IRegionJpaRepository iRegionJpaRepository;

    private NameMatcher<Region> nameMatcher;
    private RegionRepository regionRepository;

    private static WKBWriter wkbWriter;
    private static Polygon polygon;

    @BeforeAll
    private static void init(){
        wkbWriter = new WKBWriter();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        polygon = new Polygon(linearRing, new LinearRing[]{}, geometryFactory);
    }

    @BeforeEach
    public void setup(){
        this.regionRepository = new RegionRepository(iRegionJpaRepository);
    }

    @Test
    public void testSingleRegion(){
        Region region = new Region();
        region.setRegionId("55555L");
        region.setCountry("ZZ");
        region.setCode("code");
        region.setName("name");
        region.setPolygon(wkbWriter.write(polygon));

        this.iRegionJpaRepository.saveRegion(region.getRegionId(), region.getCountry(), region.getCode(),
                region.getName(), region.getPolygon());

        List<Region> results = this.regionRepository.findRegionByRegionNameAndCountry(region.getName(), region.getCountry());
        //first identify top then all with same name

        assertThat(results.size(), is(1));

        this.iRegionJpaRepository.deleteAllByRegionIdAndCountry(region.getRegionId(), region.getCountry());
    }

    @Test
    public void testMultipleRegionsWithSameName(){
        Region region1 = new Region();
        region1.setRegionId("55555L");
        region1.setCountry("ZZ");
        region1.setCode("code");
        region1.setName("name");
        region1.setPolygon(wkbWriter.write(polygon));
        Region region2 = new Region();
        region2.setRegionId("66666L");
        region2.setCountry("ZZ");
        region2.setCode("code");
        region2.setName("name");
        region2.setPolygon(wkbWriter.write(polygon));

        this.iRegionJpaRepository.saveRegion(region1.getRegionId(), region1.getCountry(), region1.getCode(),
                region1.getName(), region1.getPolygon());
        this.iRegionJpaRepository.saveRegion(region2.getRegionId(), region2.getCountry(), region2.getCode(),
                region2.getName(), region2.getPolygon());

        List<Region> results = this.regionRepository.findRegionByRegionNameAndCountry(region1.getName(), region1.getCountry());

        assertThat(results.size(), is(2));

        this.iRegionJpaRepository.deleteAllByRegionIdAndCountry(region1.getRegionId(), region1.getCountry());
        this.iRegionJpaRepository.deleteAllByRegionIdAndCountry(region2.getRegionId(), region2.getCountry());
    }
}

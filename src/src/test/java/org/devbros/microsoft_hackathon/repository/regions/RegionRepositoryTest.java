package org.devbros.microsoft_hackathon.repository.regions;

import org.devbros.microsoft_hackathon.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKBWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class RegionRepositoryTest {
    @Autowired
    private IRegionJpaRepository iRegionJpaRepository;

    private NameMatcher<Region> nameMatcher;
    private RegionRepository regionRepository;

    @BeforeEach
    public void setup(){
        this.nameMatcher = mock(NameMatcher.class);
        this.regionRepository = new RegionRepository(iRegionJpaRepository, nameMatcher);
    }

    @Test
    public void test(){
        WKBWriter wkbWriter = new WKBWriter();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        Polygon polygon = new Polygon(linearRing, new LinearRing[]{}, geometryFactory);

        Region region = new Region();
        region.setRegionId("55555L");
        region.setCountry("ZZ");
        region.setCode("code");
        region.setName("name");
        region.setPolygon(wkbWriter.write(polygon));

        AtomicInteger callsToMatcherCounter = new AtomicInteger();

        doAnswer(invocation -> {
            callsToMatcherCounter.addAndGet(1);
            return null; // return null for void methods
        }).when(nameMatcher).match(any(), any());
        when(nameMatcher.getTopMatchingEntity()).thenReturn(region);

        this.iRegionJpaRepository.saveRegion(region.getRegionId(), region.getCountry(), region.getCode(),
                region.getName(), region.getPolygon());

        List<Region> results = this.regionRepository.findRegionByRegionNameAndCountry(region.getName(), region.getCountry());
        //first identify top then all with same name

        assertThat(callsToMatcherCounter.get(), is(1));
        assertThat(results.size(), is(1));

        this.iRegionJpaRepository.deleteAllByRegionIdAndCountry(region.getRegionId(), region.getCountry());
    }
}

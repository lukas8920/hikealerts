package org.devbros.microsoft_hackathon.repository.trails;

import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.event_injection.matcher.GeoMatcher;
import org.devbros.microsoft_hackathon.event_injection.matcher.NameMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.mockito.stubbing.Answer;
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
public class TrailRepositoryTest {
    @Autowired
    private ITrailJpaRepository iTrailJpaRepository;

    private TrailRepository trailRepository;
    private WKBReader wkbReader;
    private GeoMatcher geoMatcher;
    private NameMatcher<Trail> nameMatcher;

    @BeforeEach
    public void setup(){
        this.wkbReader = new WKBReader();
        this.geoMatcher = mock(GeoMatcher.class);
        this.nameMatcher = mock(NameMatcher.class);
        this.trailRepository = new TrailRepository(this.iTrailJpaRepository, this.geoMatcher, this.nameMatcher);
    }

    @Test
    public void testTrailFindingByCodeAndCountry() throws ParseException {
        GeometryFactory geometryFactory = new GeometryFactory();
        WKBWriter wkbWriter = new WKBWriter();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LineString line = new LineString(coordinateSequence, geometryFactory);

        Trail trail1 = new Trail();
        trail1.setTrailId(55555L);
        trail1.setCoordinates(wkbWriter.write(line));
        trail1.setUnitcode("abc");
        trail1.setCountry("CC");
        Trail trail2 = new Trail();
        trail2.setTrailId(66666L);
        trail2.setUnitcode("cba");
        trail2.setCountry("ZZ");

        AtomicInteger callsToMatcherCounter = new AtomicInteger();

        doAnswer(invocation -> {
            callsToMatcherCounter.addAndGet(1);
            return null; // return null for void methods
        }).when(nameMatcher).match(any(), any());
        when(nameMatcher.getT()).thenReturn(trail1);

        iTrailJpaRepository.saveTrail(trail1.getTrailId(), trail1.getCountry(), trail1.getUnitcode(), trail1.getCoordinates());
        iTrailJpaRepository.saveTrail(trail2.getTrailId(), trail2.getCountry(), trail2.getUnitcode(), trail2.getCoordinates());

        Trail trail = this.trailRepository.searchTrailByNameUnitCodeAndCountry("dummy", trail1.getUnitcode(), trail1.getCountry());

        assertThat(trail.getTrailId(), is(55555L));
        assertThat(wkbReader.read(trail.getCoordinates()).getCoordinates()[0].x, is(1.0));
        assertThat(wkbReader.read(trail.getCoordinates()).getCoordinates()[0].y, is(1.0));
        assertThat(callsToMatcherCounter.get(), is(1));

        iTrailJpaRepository.deleteAllByTrailIdAndCountry(trail1.getTrailId(), trail1.getCountry());
        iTrailJpaRepository.deleteAllByTrailIdAndCountry(trail2.getTrailId(), trail2.getCountry());
    }

    @Test
    public void testThatPaginationWorks(){
        GeometryFactory geometryFactory = new GeometryFactory();
        WKBWriter wkbWriter = new WKBWriter();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LineString line = new LineString(coordinateSequence, geometryFactory);

        Trail trail1 = new Trail();
        trail1.setTrailId(55555L);
        trail1.setCoordinates(wkbWriter.write(line));
        trail1.setUnitcode("abc");
        trail1.setCountry("ZZ");
        Trail trail2 = new Trail();
        trail2.setTrailId(66666L);
        trail2.setUnitcode("cba");
        trail2.setCountry("ZZ");
        trail2.setCoordinates(wkbWriter.write(line));
        Trail trail3 = new Trail();
        trail3.setTrailId(77777L);
        trail3.setUnitcode("ccc");
        trail3.setCountry("ZZ");
        trail3.setCoordinates(wkbWriter.write(line));

        AtomicInteger callsToMatcherCounter = new AtomicInteger();

        iTrailJpaRepository.saveTrail(trail1.getTrailId(), trail1.getCountry(), trail1.getUnitcode(), trail1.getCoordinates());
        iTrailJpaRepository.saveTrail(trail2.getTrailId(), trail2.getCountry(), trail2.getUnitcode(), trail2.getCoordinates());
        iTrailJpaRepository.saveTrail(trail3.getTrailId(), trail3.getCountry(), trail3.getUnitcode(), trail3.getCoordinates());

        when(geoMatcher.match(any(), any())).then((Answer<List<Trail>>) invocationOnMock -> {
            callsToMatcherCounter.addAndGet(1);
            return List.of(new Trail());
        });

        List<Trail> trails = this.trailRepository.findTrailsInRegion(null, "ZZ");

        assertThat(callsToMatcherCounter.get(), is(3));
        assertThat(trails.size(), is(3));

        iTrailJpaRepository.deleteAllByTrailIdAndCountry(trail1.getTrailId(), trail1.getCountry());
        iTrailJpaRepository.deleteAllByTrailIdAndCountry(trail2.getTrailId(), trail2.getCountry());
        iTrailJpaRepository.deleteAllByTrailIdAndCountry(trail3.getTrailId(), trail3.getCountry());
    }
}

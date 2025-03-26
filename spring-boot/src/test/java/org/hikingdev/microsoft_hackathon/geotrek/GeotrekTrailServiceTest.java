package org.hikingdev.microsoft_hackathon.geotrek;

import jakarta.persistence.EntityManager;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GeoMatcher;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailJpaRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.TrailRepository;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GeotrekTrailServiceTest {
    private GeotrekTrailService geotrekTrailService;
    private GeonamesService geonamesService;
    private IPublisherRepository iPublisherRepository;
    private TrailMapper trailMapper;

    @BeforeEach
    public void setup(){
        this.geonamesService = mock(GeonamesService.class);
        ITrailRepository iTrailRepository = mock(ITrailRepository.class);
        this.iPublisherRepository = mock(IPublisherRepository.class);
        this.trailMapper = new TrailMapper();
        this.geotrekTrailService = new GeotrekTrailService("dummy", geonamesService, trailMapper, iTrailRepository, iPublisherRepository);
    }

    @Test
    public void testThatPersistEditorHandlesInvalidName(){
        GeotrekTrail geotrekTrail1 = new GeotrekTrail();
        geotrekTrail1.setName(null);

        Exception exception1 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistEditorData(geotrekTrail1));
        assertThat(exception1.getMessage(), is("Empty name cannot be referenced in the database."));

        GeotrekTrail geotrekTrail2 = new GeotrekTrail();
        geotrekTrail2.setName(" ");

        Exception exception2 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistEditorData(geotrekTrail2));
        assertThat(exception2.getMessage(), is("Empty name cannot be referenced in the database."));
    }

    @Test
    public void testThatPersistEditorHandlesInvalidCountry() throws BadRequestException {
        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3)});
        GeotrekTrail geotrekTrail = new GeotrekTrail();
        geotrekTrail.setName("test");
        geotrekTrail.setCoordinates(new LineString(coordinateSequence, geometryFactory));

        when(this.geonamesService.countryCode(2, 2, "dummy")).thenReturn(null);

        Exception exception1 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistEditorData(geotrekTrail));
        assertThat(exception1.getMessage(), is("No valid country returned by geoname service."));

        GeonamesResponse geonamesResponse1 = new GeonamesResponse();
        geonamesResponse1.setCountryCode("ddd");

        when(this.geonamesService.countryCode(2, 2, "dummy")).thenReturn(geonamesResponse1);

        Exception exception2 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistEditorData(geotrekTrail));
        assertThat(exception2.getMessage(), is("No valid country returned by geoname service."));
    }

    @Test
    public void testThatPersistGeotrekTrailWorks() throws BadRequestException {
        ITrailJpaRepository iTrailJpaRepository = mock(ITrailJpaRepository.class);
        GeoMatcher geoMatcher = mock(GeoMatcher.class);
        EntityManager entityManager = mock(EntityManager.class);
        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(iTrailJpaRepository, geoMatcher, entityManager);
        GeotrekTrailService geotrekTrailService = new GeotrekTrailService("dummy", this.geonamesService, this.trailMapper, trailRepositoryCallback, this.iPublisherRepository);

        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3)});
        GeotrekTrail geotrekTrail = new GeotrekTrail();
        geotrekTrail.setName("test");
        geotrekTrail.setId("trail_1");
        geotrekTrail.setMaintainer("maintainer");
        geotrekTrail.setCoordinates(new LineString(coordinateSequence, geometryFactory));

        GeonamesResponse geonamesResponse = new GeonamesResponse();
        geonamesResponse.setCountryCode("DE");

        when(this.geonamesService.countryCode(anyDouble(), anyDouble(), eq("dummy"))).thenReturn(geonamesResponse);

        geotrekTrailService.persistEditorData(geotrekTrail);

        assertThat(trailRepositoryCallback.counter, is(0));
        assertThat(trailRepositoryCallback.trail.getTrailId(), is("trail_1"));
        assertThat(trailRepositoryCallback.trail.getTrailname(), is("test"));
        assertThat(trailRepositoryCallback.trail.getMaintainer(), is("maintainer"));
        assertThat(trailRepositoryCallback.trail.getCountry(), is("DE"));
        assertThat(trailRepositoryCallback.trail.getCoordinates().length > 1, is(true));
    }

    @Test
    public void testInvalidIds(){
        List<String> ids1 = null;

        Exception e = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.deleteTrails(ids1));
        assertThat(e.getMessage(), is("No valid ids for delete request provided"));

        List<String> ids2 = new ArrayList<>();

        e = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.deleteTrails(ids2));
        assertThat(e.getMessage(), is("No valid ids for delete request provided"));

        List<String> ids3 = List.of("a", "b", "c", "d");

        e = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.deleteTrails(ids3));
        assertThat(e.getMessage(), is("No valid ids for delete request provided"));
    }

    @Test
    public void testThatDeleteTrailsWorks() throws BadRequestException {
        ITrailJpaRepository iTrailJpaRepository = mock(ITrailJpaRepository.class);
        GeoMatcher geoMatcher = mock(GeoMatcher.class);
        EntityManager entityManager = mock(EntityManager.class);
        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(iTrailJpaRepository, geoMatcher, entityManager);
        IPublisherRepository iPublisherRepository = mock(IPublisherRepository.class);
        Publisher publisher = new Publisher();
        publisher.setName("dummy");

        List<String> ids = List.of("2L");

        GeotrekTrailService geotrekTrailService = spy(new GeotrekTrailService(null, null, null, trailRepositoryCallback, iPublisherRepository));

        doReturn(1L).when(geotrekTrailService).getActiveSecurityContextHolder();
        doReturn(publisher).when(iPublisherRepository).findUserById(1L);

        geotrekTrailService.deleteTrails(ids);

        assertThat(trailRepositoryCallback.counter, is(0));
        assertThat(trailRepositoryCallback.publishers.size(), is(2));
        assertThat(trailRepositoryCallback.publishers.contains(publisher.getName()), is(true));
        assertThat(trailRepositoryCallback.publishers.contains("Community"), is(true));
        assertThat(trailRepositoryCallback.trail_id, is("2L"));
    }

    static class TrailRepositoryCallback extends TrailRepository {
        String trail_id;
        List<String> publishers;
        Trail trail;
        int counter = 1;

        public TrailRepositoryCallback(ITrailJpaRepository iTrailJpaRepository, GeoMatcher geoMatcher,
                                       EntityManager entityManager) {
            super(iTrailJpaRepository, geoMatcher, entityManager);
        }

        @Override
        public void save(Trail trail){
            this.trail = trail;
            counter -= 1;
        }

        @Override
        public void delete(String trail_id, List<String> publishers){
            this.trail_id = trail_id;
            this.publishers = publishers;
            counter -= 1;
        }
    }
}

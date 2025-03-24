package org.hikingdev.microsoft_hackathon.geotrek;

import jakarta.persistence.EntityManager;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GeoMatcher;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GeotrekTrailServiceTest {
    private GeotrekTrailService geotrekTrailService;
    private GeonamesService geonamesService;
    private TrailMapper trailMapper;

    @BeforeEach
    public void setup(){
        this.geonamesService = mock(GeonamesService.class);
        ITrailRepository iTrailRepository = mock(ITrailRepository.class);
        this.trailMapper = new TrailMapper();
        this.geotrekTrailService = new GeotrekTrailService("dummy", geonamesService, trailMapper, iTrailRepository);
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
        GeotrekTrailService geotrekTrailService = new GeotrekTrailService("dummy", this.geonamesService, this.trailMapper, trailRepositoryCallback);

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

    static class TrailRepositoryCallback extends TrailRepository {
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
    }
}

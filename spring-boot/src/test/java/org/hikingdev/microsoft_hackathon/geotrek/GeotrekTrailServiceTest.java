package org.hikingdev.microsoft_hackathon.geotrek;

import jakarta.persistence.EntityManager;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.matcher.GeoMatcher;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeotrekDbService;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekUser;
import org.hikingdev.microsoft_hackathon.geotrek.entities.Salt;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailJpaRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.TrailRepository;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.geodata.Math;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

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
    private GeotrekDbService geotrekDbService;

    @BeforeEach
    public void setup(){
        this.geonamesService = mock(GeonamesService.class);
        ITrailRepository iTrailRepository = mock(ITrailRepository.class);
        this.iPublisherRepository = mock(IPublisherRepository.class);
        this.trailMapper = new TrailMapper();
        this.geotrekDbService = mock(GeotrekDbService.class);
        this.geotrekTrailService = new GeotrekTrailService("dummy", geonamesService, trailMapper, iTrailRepository, iPublisherRepository, geotrekDbService);
    }

    @Test
    public void testThatPersistEditorHandlesInvalidName(){
        GeotrekTrail geotrekTrail1 = new GeotrekTrail();
        geotrekTrail1.setName(null);

        Exception exception1 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrail(geotrekTrail1));
        assertThat(exception1.getMessage(), is("Empty name cannot be referenced in the database."));

        GeotrekTrail geotrekTrail2 = new GeotrekTrail();
        geotrekTrail2.setName(" ");

        Exception exception2 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrail(geotrekTrail2));
        assertThat(exception2.getMessage(), is("Empty name cannot be referenced in the database."));
    }

    @Test
    public void testThatPersistEditorHandlesInvalidCountry() throws IOException {
        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3)});
        GeotrekTrail geotrekTrail = new GeotrekTrail();
        geotrekTrail.setName("test");
        geotrekTrail.setCoordinates(new LineString(coordinateSequence, geometryFactory));

        Call<GeonamesResponse> call = mock(Call.class);
        Response<GeonamesResponse> response = mock(Response.class);

        when(this.geonamesService.countryCode(anyDouble(), anyDouble(), eq("dummy"))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(null);

        Exception exception1 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrail(geotrekTrail));
        assertThat(exception1.getMessage(), is("No valid country returned by geoname service."));

        GeonamesResponse geonamesResponse1 = new GeonamesResponse();
        geonamesResponse1.setCountryCode("ddd");

        when(this.geonamesService.countryCode(2, 2, "dummy")).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(geonamesResponse1);

        Exception exception2 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrail(geotrekTrail));
        assertThat(exception2.getMessage(), is("No valid country returned by geoname service."));
    }

    @Test
    public void testThatPersistGeotrekTrailWorks() throws BadRequestException, IOException {
        ITrailJpaRepository iTrailJpaRepository = mock(ITrailJpaRepository.class);
        GeoMatcher geoMatcher = mock(GeoMatcher.class);
        EntityManager entityManager = mock(EntityManager.class);
        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(iTrailJpaRepository, geoMatcher, entityManager);
        GeotrekTrailService geotrekTrailService = new GeotrekTrailService("dummy", this.geonamesService, this.trailMapper, trailRepositoryCallback, this.iPublisherRepository, this.geotrekDbService);

        GeometryFactory geometryFactory = new GeometryFactory();
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2), new Coordinate(3, 3)});
        GeotrekTrail geotrekTrail = new GeotrekTrail();
        geotrekTrail.setName("test");
        geotrekTrail.setId("geotrek-1");
        geotrekTrail.setMaintainer("maintainer");
        geotrekTrail.setCoordinates(new LineString(coordinateSequence, geometryFactory));

        Call<GeonamesResponse> call = mock(Call.class);
        Response<GeonamesResponse> response = mock(Response.class);

        GeonamesResponse geonamesResponse = new GeonamesResponse();
        geonamesResponse.setCountryCode("DE");

        when(this.geonamesService.countryCode(anyDouble(), anyDouble(), eq("dummy"))).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(geonamesResponse);

        List<GeotrekTrail> geotrekTrails = List.of(geotrekTrail);
        this.mockGeotrekDbServiceFindTrails(geotrekTrails, 1L);

        geotrekTrailService.persistTrail(geotrekTrail);

        assertThat(trailRepositoryCallback.counter, is(0));
        assertThat(trailRepositoryCallback.trail.getTrailId(), is("geotrek-1"));
        assertThat(trailRepositoryCallback.trail.getTrailname(), is("test"));
        assertThat(trailRepositoryCallback.trail.getMaintainer(), is("maintainer"));
        assertThat(trailRepositoryCallback.trail.getCountry(), is("DE"));
        assertThat(trailRepositoryCallback.trail.getCoordinates().length > 1, is(true));
    }

    @Test
    public void testInvalidIds(){
        String id1 = null;

        Exception e = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.deleteTrail(id1));
        assertThat(e.getMessage(), is("No valid ids for delete request provided"));
    }

    @Test
    public void testThatDeleteTrailsSavesNewPath() throws BadRequestException, IOException {
        ITrailJpaRepository iTrailJpaRepository = mock(ITrailJpaRepository.class);
        GeoMatcher geoMatcher = mock(GeoMatcher.class);
        EntityManager entityManager = mock(EntityManager.class);
        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(iTrailJpaRepository, geoMatcher, entityManager);
        IPublisherRepository iPublisherRepository = mock(IPublisherRepository.class);
        Publisher publisher = new Publisher();
        publisher.setName("dummy");

        String id = "geotrek-2";

        GeotrekTrailService geotrekTrailService = spy(new GeotrekTrailService(null, null, trailMapper, trailRepositoryCallback, iPublisherRepository, geotrekDbService));

        doReturn(1L).when(geotrekTrailService).getActiveSecurityContextHolder();
        doReturn(publisher).when(iPublisherRepository).findPublisherByUserId(1L);

        Call<List<GeotrekTrail>> call = mock(Call.class);
        Response<List<GeotrekTrail>> response = mock(Response.class);

        List<GeotrekTrail> geotrekTrails = Arrays.asList(new GeotrekTrail(), new GeotrekTrail());

        when(this.geotrekDbService.findTrails(2L)).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(geotrekTrails);

        GeotrekTrail geotrekTrail = new GeotrekTrail();
        geotrekTrail.setId("3");
        geotrekTrail.setCoordinates(epsg3857LineString());
        doReturn(geotrekTrail).when(geotrekTrailService).joinGeotrekTrails(geotrekTrails);

        geotrekTrailService.deleteTrail(id);

        assertThat(trailRepositoryCallback.counter, is(-1));
        assertThat(trailRepositoryCallback.publishers.size(), is(2));
        assertThat(trailRepositoryCallback.publishers.contains(publisher.getName()), is(true));
        assertThat(trailRepositoryCallback.publishers.contains("Community"), is(true));
        assertThat(trailRepositoryCallback.trail_id, is("geotrek-2"));
        assertThat(trailRepositoryCallback.trail.getTrailId(), is("geotrek-3"));
        assertThat(trailRepositoryCallback.trail.getCountry(), is("ZZ"));
    }

    @Test
    public void testThatDeleteIsComplete() throws IOException, BadRequestException {
        ITrailJpaRepository iTrailJpaRepository = mock(ITrailJpaRepository.class);
        GeoMatcher geoMatcher = mock(GeoMatcher.class);
        EntityManager entityManager = mock(EntityManager.class);
        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(iTrailJpaRepository, geoMatcher, entityManager);
        IPublisherRepository iPublisherRepository = mock(IPublisherRepository.class);
        Publisher publisher = new Publisher();
        publisher.setName("dummy");

        String id = "geotrek-2";

        GeotrekTrailService geotrekTrailService = spy(new GeotrekTrailService(null, null, trailMapper, trailRepositoryCallback, iPublisherRepository, geotrekDbService));

        doReturn(1L).when(geotrekTrailService).getActiveSecurityContextHolder();
        doReturn(publisher).when(iPublisherRepository).findPublisherByUserId(1L);

        List<GeotrekTrail> geotrekTrails = List.of(new GeotrekTrail());
        mockGeotrekDbServiceFindTrails(geotrekTrails, 2L);

        geotrekTrailService.deleteTrail(id);

        assertThat(trailRepositoryCallback.counter, is(0));
        assertThat(trailRepositoryCallback.publishers.size(), is(2));
        assertThat(trailRepositoryCallback.publishers.contains(publisher.getName()), is(true));
        assertThat(trailRepositoryCallback.publishers.contains("Community"), is(true));
        assertThat(trailRepositoryCallback.trail_id, is("geotrek-2"));
    }

    @Test
    public void testThatImportTrailRejectsInvalidLists(){
        List<GeotrekTrail> trails1 = null;
        Exception exception1 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrails(trails1));
        assertThat(exception1.getMessage(), is("Invalid input for multiple geotrek trails."));

        List<GeotrekTrail> trails2 = List.of();
        Exception exception2 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrails(trails2));
        assertThat(exception2.getMessage(), is("Invalid input for multiple geotrek trails."));

        List<GeotrekTrail> trails3 = new ArrayList<>();
        trails3.addAll(Collections.nCopies(201, new GeotrekTrail()));
        Exception exception3 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrails(trails3));
        assertThat(exception3.getMessage(), is("Invalid input for multiple geotrek trails."));
    }

    @Test
    public void testThatImportTrailsRejectsInvalidFormat(){
        LineString lineString1 = epsg3857LineString();
        GeotrekTrail geotrekTrail1 = new GeotrekTrail(null, "dummy", "maintainer", lineString1, 0, 0, null);

        Exception exception1 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrails(List.of(geotrekTrail1)));

        assertThat(exception1.getMessage(), is("Linestring format for index 0 needs to be WGS84"));

        LineString lineString2 = wgs84LineString();
        GeotrekTrail geotrekTrail2 = new GeotrekTrail(null, "dummy", "maintainer", lineString2, 0, 0, null);

        Exception exception2 = assertThrows(BadRequestException.class, () -> this.geotrekTrailService.persistTrails(List.of(geotrekTrail2)));

        assertThat(exception2.getMessage(), is("Trail with index 0 does not specify a country."));
    }

    @Test
    public void testThatImportTrailsWorks() throws BadRequestException, ParseException, IOException {
        LineString lineString = wgs84LineString();
        GeotrekTrail geotrekTrail = new GeotrekTrail(null, "dummy", "maintainer", lineString, 0, 0, "ZZ");

        Call<GeotrekTrail> geotrekCall = mock(Call.class);
        Response<GeotrekTrail> response = mock(Response.class);
        GeotrekTrail outputTrail = new GeotrekTrail();
        outputTrail.setId("geotrek-1");
        outputTrail.setCoordinates(lineString);
        when(geotrekCall.execute()).thenReturn(response);
        when(response.body()).thenReturn(outputTrail);

        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(null, null, null);
        GeotrekDbCallback geotrekDbCallback = new GeotrekDbCallback(geotrekCall);

        GeotrekTrailService geotrekTrailService = new GeotrekTrailService(null, null, this.trailMapper,
                trailRepositoryCallback, null, geotrekDbCallback);

        geotrekTrailService.persistTrails(List.of(geotrekTrail));

        assertThat(geotrekDbCallback.counter, is(0));
        assertThat(geotrekDbCallback.geotrekTrail.getName(), is("dummy"));
        assertThat(geotrekDbCallback.geotrekTrail.getMaintainer(), is("maintainer"));
        assertThat(Math.isValidEPSG3857(geotrekDbCallback.geotrekTrail.getCoordinates()), is(true));

        assertThat(trailRepositoryCallback.counter, is(0));
        assertThat(trailRepositoryCallback.trail.getTrailId(), is("geotrek-1"));
        assertThat(trailRepositoryCallback.trail.getTrailname(), is("dummy"));
        assertThat(trailRepositoryCallback.trail.getMaintainer(), is("maintainer"));
        assertThat(trailRepositoryCallback.trail.getCountry(), is("ZZ"));

        WKBReader wkbReader = new WKBReader();
        LineString resultLinestring = (LineString) wkbReader.read(trailRepositoryCallback.trail.getCoordinates());
        assertThat(Math.isValidWGS84(resultLinestring), is(true));
    }

    @Test
    public void testImportTrailsDeletesCorrectly() throws IOException, BadRequestException {
        LineString lineString1 = extendedWgs84LineString();
        GeotrekTrail inputTrail = new GeotrekTrail(null, "dummy", "maintainer", lineString1, 0, 0, "ZZ");

        LineString lineString2 = wgs84LineString();
        GeotrekTrail persistedTrail = new GeotrekTrail("1", "dummy", "maintainer", lineString2, 0, 0, "ZZ");

        Call<GeotrekTrail> firstGeotrekCall = mock(Call.class);
        Response<GeotrekTrail> response1 = mock(Response.class);
        when(firstGeotrekCall.execute()).thenReturn(response1);
        when(response1.body()).thenReturn(persistedTrail);

        Call<GeotrekTrail> secondGeotrekCall = mock(Call.class);
        Response<GeotrekTrail> response2 = mock(Response.class);
        GeotrekTrail outputTrail = new GeotrekTrail();
        outputTrail.setId("geotrek-1");
        outputTrail.setCoordinates(lineString1);
        outputTrail.setCountry("ZZ");
        when(secondGeotrekCall.execute()).thenReturn(response2);
        when(response2.body()).thenReturn(outputTrail);

        Call<List<GeotrekTrail>> trailsCallback = mock(Call.class);
        Response<List<GeotrekTrail>> response3 = mock(Response.class);
        when(trailsCallback.execute()).thenReturn(response3);
        when(response3.body()).thenReturn(List.of(persistedTrail));

        Call<Void> deleteCallback = mock(Call.class);

        TrailRepositoryCallback trailRepositoryCallback = new TrailRepositoryCallback(null, null, null);
        GeotrekDbCallback geotrekDbCallback = new GeotrekDbCallback(firstGeotrekCall, secondGeotrekCall, trailsCallback, deleteCallback);

        GeotrekTrailService geotrekTrailService = new GeotrekTrailService(null, null, this.trailMapper,
                trailRepositoryCallback, null, geotrekDbCallback);

        geotrekTrailService.persistTrails(List.of(inputTrail));

        // check that there are two calls to geotrek db service
        assertThat(geotrekDbCallback.counter, is(-1));

        // check that valid geotrek trail is returned
        assertThat(geotrekDbCallback.geotrekTrail.getName(), is("dummy"));
        assertThat(geotrekDbCallback.geotrekTrail.getMaintainer(), is("maintainer"));
        assertThat(Math.isValidEPSG3857(geotrekDbCallback.geotrekTrail.getCoordinates()), is(true));

        // check that there are two calls to trail repository
        assertThat(trailRepositoryCallback.counter, is(-1));

        // check that new trail is inserted into the database
        assertThat(trailRepositoryCallback.trail.getTrailId(), is("geotrek-1"));
        assertThat(trailRepositoryCallback.trail.getTrailname(), is("dummy"));
        assertThat(trailRepositoryCallback.trail.getMaintainer(), is("maintainer"));
        assertThat(trailRepositoryCallback.trail.getCountry(), is("ZZ"));

        // check that trail existing trail is deleted
        assertThat(trailRepositoryCallback.trail_id, is("geotrek-1"));
    }

    private void mockGeotrekDbServiceFindTrails(List<GeotrekTrail> geotrekTrails, Long id) throws IOException {
        Call<List<GeotrekTrail>> call = mock(Call.class);
        Response<List<GeotrekTrail>> response = mock(Response.class);

        when(this.geotrekDbService.findTrails(id)).thenReturn(call);
        when(call.execute()).thenReturn(response);
        when(response.body()).thenReturn(geotrekTrails);
    }

    LineString wgs84LineString(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(-74.006, 40.7128),
                new Coordinate(90, 0),
                new Coordinate(0, 51.5074)
        };
        CoordinateSequence seq = new CoordinateArraySequence(coordinates);
        return new LineString(seq, geometryFactory);
    }

    LineString extendedWgs84LineString(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{
                new Coordinate(-74.006, 40.7128),
                new Coordinate(90, 0),
                new Coordinate(0, 51.5074),
                new Coordinate(0, 69)
        };
        CoordinateSequence seq = new CoordinateArraySequence(coordinates);
        return new LineString(seq, geometryFactory);
    }

    LineString epsg3857LineString(){
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates1 = new Coordinate[]{
                new Coordinate(-8238310.234500223, 4970071.579142425),
                new Coordinate(1.001875417E7, -7.081154551613622E-10),
                new Coordinate(0.0, 6711542.475587636)
        };
        CoordinateSequence seq1 = new CoordinateArraySequence(coordinates1);
        return new LineString(seq1, geometryFactory);
    }

    static class GeotrekDbCallback implements GeotrekDbService {
        GeotrekTrail geotrekTrail;
        int counter = 1;

        private final Call<GeotrekTrail> firstPostResponse;
        private final Call<GeotrekTrail> secondPostResponse;
        private final Call<List<GeotrekTrail>> trailCallback;
        private final Call<Void> deleteCallback;

        public GeotrekDbCallback(Call<GeotrekTrail> firstPostResponse){
            this.secondPostResponse = null;
            this.trailCallback = null;
            this.deleteCallback = null;
            this.firstPostResponse = firstPostResponse;
        }

        public GeotrekDbCallback(Call<GeotrekTrail> firstPostResponse, Call<GeotrekTrail> secondPostResponse,
                                 Call<List<GeotrekTrail>> trailCallback, Call<Void> deleteCallback){
            this.firstPostResponse = firstPostResponse;
            this.secondPostResponse = secondPostResponse;
            this.trailCallback = trailCallback;
            this.deleteCallback = deleteCallback;
        }

        @Override
        public Call<Void> postUser(GeotrekUser geotrekUser) {
            return null;
        }

        @Override
        public Call<Salt> getSalt() {
            return null;
        }

        @Override
        public Call<GeotrekTrail> postTrail(GeotrekTrail geotrekTrail) {
            this.geotrekTrail = geotrekTrail;
            this.counter -= 1;
            if (this.counter == 0){
                return this.firstPostResponse;
            } else if (this.counter == -1){
                return this.secondPostResponse;
            }
            return null;
        }

        @Override
        public Call<List<GeotrekTrail>> findTrails(Long id) {
            return this.trailCallback;
        }

        @Override
        public Call<Void> deleteTrails(List<String> ids) {
            return this.deleteCallback;
        }
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
        public int delete(String trail_id, List<String> publishers){
            this.trail_id = trail_id;
            this.publishers = publishers;
            counter -= 1;
            return 1;
        }

        @Override
        public Trail findTrailByTrailId(String trailId){
            Trail trail = new Trail();
            trail.setCountry("ZZ");
            return trail;
        }
    }
}

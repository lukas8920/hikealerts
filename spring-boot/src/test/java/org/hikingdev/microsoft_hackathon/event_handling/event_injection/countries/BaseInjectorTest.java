package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.regions.IRegionRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseInjectorTest {
    protected IRawEventRepository iRawEventRepository;
    protected IEventRepository iEventRepository;
    protected ITrailRepository iTrailRepository;
    protected IRegionRepository iRegionRepository;

    private LineString line;

    protected BaseCountryInjector injector;
    protected String country;

    @BeforeEach
    public void setup(){
        this.iRawEventRepository = mock(IRawEventRepository.class);
        this.iEventRepository = mock(IEventRepository.class);
        this.iTrailRepository = mock(ITrailRepository.class);
        this.iRegionRepository = mock(IRegionRepository.class);

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        line = new LineString(coordinateSequence, geometryFactory);
    }

    protected abstract void mockTestThatMatchTrailsWorksForTrailname(OpenAiEvent openAiEvent, RawEvent rawEvent, Trail trail);

    @Test
    public void testThatMatchTrailsWorksForTrailname() throws ParseException {
        WKBWriter wkbWriter = new WKBWriter();
        RawEvent rawEvent = new RawEvent();
        rawEvent.setUnitCode("abc");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry(this.country);
        openAiEvent.setTrailName("dummy");
        Trail trail = new Trail();
        trail.setId(1L);
        trail.setCoordinates(wkbWriter.write(line));

        this.mockTestThatMatchTrailsWorksForTrailname(openAiEvent, rawEvent, trail);

        boolean flag = injector.matchTrails(openAiEvent);

        assertThat(flag, is(true));
    }

    protected abstract void mockTestThatMatchTrailsWorksForTrailFoundViaRegion(RawEvent rawEvent, Region region, Trail trail);

    @Test
    public void testThatMatchTrailsWorksForTrailFoundViaRegion() throws ParseException {
        GeometryFactory geometryFactory = new GeometryFactory();
        WKBWriter wkbWriter = new WKBWriter();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        Polygon polygon = new Polygon(linearRing, new LinearRing[]{}, geometryFactory);

        RawEvent rawEvent = new RawEvent();
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry(this.country);
        openAiEvent.setRegion("region");
        Region region = new Region();
        region.setCode("abc");
        region.setCountry(this.country);
        region.setPolygon(wkbWriter.write(polygon));
        Trail trail = new Trail();
        trail.setCoordinates(wkbWriter.write(line));

        this.mockTestThatMatchTrailsWorksForTrailFoundViaRegion(rawEvent, region, trail);

        boolean flag = injector.matchTrails(openAiEvent);

        assertThat(flag, is(true));
    }

    protected abstract void mockTestThatMatchTrailsQuitsForEmptyEvents(OpenAiEvent openAiEvent, RawEvent rawEvent);

    @Test
    public void testThatMatchTrailsQuitsForEmptyEvents() throws ParseException {
        RawEvent rawEvent = new RawEvent();
        rawEvent.setUnitCode("abc");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry(this.country);
        openAiEvent.setTrailName("dummy");

        this.mockTestThatMatchTrailsQuitsForEmptyEvents(openAiEvent, rawEvent);

        boolean flag = injector.matchTrails(openAiEvent);

        assertThat(flag, is(false));
    }

    @Test
    public void testThatMatchTrailsQuitsForNullRawEvent() throws ParseException {
        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(null);
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry(this.country);

        boolean result = this.injector.matchTrails(openAiEvent);

        assertThat(result, is(false));
    }

    protected abstract void mockTestThatDisplayMidCoordinateWorks(Region region, Trail trail);

    @Test
    public void testThatDisplayMidCoordinateWorks() throws ParseException {
        WKBWriter wkbWriter = new WKBWriter();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        Polygon polygon = new Polygon(linearRing, new LinearRing[]{}, geometryFactory);

        Region region = new Region();
        region.setCountry(this.country);
        region.setPolygon(wkbWriter.write(polygon));
        Trail trail = new Trail();
        trail.setCoordinates(wkbWriter.write(line));
        Event event = new Event();
        event.setRegion("region");
        event.setCountry(this.country);

        this.mockTestThatDisplayMidCoordinateWorks(region, trail);

        List<Event> events = this.injector.identifyTrailsViaRegion(event);

        assertThat(events.get(0).getMidLatitudeCoordinate(), is(1.0));
        assertThat(events.get(0).getMidLongitudeCoordinate(), is(1.0));
        assertThat(events.get(0).getTrailIds().size(), is(1));
    }
}

package org.devbros.microsoft_hackathon.event_handling.event_injection.countries;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.devbros.microsoft_hackathon.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class USInjectorTest {
    private IRawEventRepository iRawEventRepository;
    private IEventRepository iEventRepository;
    private ITrailRepository iTrailRepository;
    private IRegionRepository iRegionRepository;

    private LineString line;

    private USInjector usInjector;

    @BeforeEach
    public void setup(){
        this.iRawEventRepository = mock(IRawEventRepository.class);
        this.iEventRepository = mock(IEventRepository.class);
        this.iTrailRepository = mock(ITrailRepository.class);
        this.iRegionRepository = mock(IRegionRepository.class);
        this.usInjector = new USInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);

        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        line = new LineString(coordinateSequence, geometryFactory);
    }

    @Test
    public void testThatMatchTrailsWorksForTrailname() throws ParseException {
        WKBWriter wkbWriter = new WKBWriter();
        RawEvent rawEvent = new RawEvent();
        rawEvent.setUnitCode("abc");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry("US");
        openAiEvent.setTrailName("dummy");
        Trail trail = new Trail();
        trail.setId(1L);
        trail.setCoordinates(wkbWriter.write(line));

        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameUnitCodeAndCountry(openAiEvent.getTrailName(), "abc", "US")).thenReturn(trail);

        boolean flag = usInjector.matchTrails(openAiEvent);

        assertThat(flag, is(true));
    }

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
        openAiEvent.setCountry("US");
        openAiEvent.setRegion("region");
        Region region = new Region();
        region.setCode("abc");
        region.setCountry("US");
        region.setPolygon(wkbWriter.write(polygon));
        Trail trail = new Trail();
        trail.setCoordinates(wkbWriter.write(line));

        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(rawEvent);
        when(iRegionRepository.findUniqueRegionName("region", "US")).thenReturn(List.of(region));
        when(iTrailRepository.findTrailsByNameCodeAndCountry(any(), eq("US"), any())).thenReturn(List.of(trail));

        boolean flag = usInjector.matchTrails(openAiEvent);

        assertThat(flag, is(true));
    }

    @Test
    public void testThatMatchTrailsQuitsForEmptyEvents() throws ParseException {
        RawEvent rawEvent = new RawEvent();
        rawEvent.setUnitCode("abc");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry("US");
        openAiEvent.setTrailName("dummy");

        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(rawEvent);
        when(iTrailRepository.searchTrailByNameUnitCodeAndCountry(openAiEvent.getTrailName() , "abc", "US")).thenReturn(null);

        boolean flag = usInjector.matchTrails(openAiEvent);

        assertThat(flag, is(false));
    }

    @Test
    public void testThatMatchTrailsQuitsForNullRawEvent() throws ParseException {
        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(null);
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry("US");

        boolean result = this.usInjector.matchTrails(openAiEvent);

        assertThat(result, is(false));
    }

    @Test
    public void testThatDisplayMidCoordinateWorks() throws ParseException {
        WKBWriter wkbWriter = new WKBWriter();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(1, 1), new Coordinate(1, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, geometryFactory);
        Polygon polygon = new Polygon(linearRing, new LinearRing[]{}, geometryFactory);

        Region region = new Region();
        region.setCountry("US");
        region.setPolygon(wkbWriter.write(polygon));
        Trail trail = new Trail();
        trail.setCoordinates(wkbWriter.write(line));
        Event event = new Event();
        event.setRegion("region");
        event.setCountry("US");

        when(iRegionRepository.findUniqueRegionName("region", "US")).thenReturn(List.of(region));
        when(iTrailRepository.findTrailsByNameCodeAndCountry(any(), eq("US"), any())).thenReturn(List.of(trail));

        List<Event> events = this.usInjector.identifyTrailsViaRegion(event);

        assertThat(events.get(0).getMidLatitudeCoordinate(), is(1.0));
        assertThat(events.get(0).getMidLongitudeCoordinate(), is(1.0));
        assertThat(events.get(0).getTrailIds().size(), is(1));
    }
}

package org.devbros.microsoft_hackathon.event_injection.countries;

import org.devbros.microsoft_hackathon.event_injection.entities.OpenAiEvent;
import org.devbros.microsoft_hackathon.event_injection.entities.RawEvent;
import org.devbros.microsoft_hackathon.event_injection.entities.Region;
import org.devbros.microsoft_hackathon.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.event_injection.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.event_injection.repository.trails.ITrailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    public void testThatMatchTrailsWorksForTrailname(){
        RawEvent rawEvent = new RawEvent();
        rawEvent.setUnitCode("abc");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry("US");
        openAiEvent.setTrailName("dummy");
        Trail trail = new Trail();
        trail.setLine(line);

        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(rawEvent);
        when(iTrailRepository.findTrailByUnitCodeAndCountry("abc", "US")).thenReturn(trail);

        boolean flag = usInjector.matchTrails(openAiEvent);

        assertThat(flag, is(true));
    }

    @Test
    public void testThatMatchTrailsWorksForTrailFoundViaRegion(){
        GeometryFactory geometryFactory = new GeometryFactory();
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
        region.setPolygon(polygon);
        Trail trail = new Trail();
        trail.setLine(line);

        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(rawEvent);
        when(iRegionRepository.findRegionByRegionName("region")).thenReturn(region);
        when(iTrailRepository.findTrailsInRegion(region.getPolygon())).thenReturn(List.of(trail));

        boolean flag = usInjector.matchTrails(openAiEvent);

        assertThat(flag, is(true));
    }

    @Test
    public void testThatMatchTrailsQuitsForEmptyEvents(){
        RawEvent rawEvent = new RawEvent();
        rawEvent.setUnitCode("abc");
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry("US");
        openAiEvent.setTrailName("dummy");

        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(rawEvent);
        when(iTrailRepository.findTrailByUnitCodeAndCountry("abc", "US")).thenReturn(null);

        boolean flag = usInjector.matchTrails(openAiEvent);

        assertThat(flag, is(false));
    }

    @Test
    public void testThatMatchTrailsQuitsForNullRawEvent(){
        when(iRawEventRepository.findRawEvent("1", "US")).thenReturn(null);
        OpenAiEvent openAiEvent = new OpenAiEvent();
        openAiEvent.setEventId("1");
        openAiEvent.setCountry("US");

        boolean result = this.usInjector.matchTrails(openAiEvent);

        assertThat(result, is(false));
    }
}

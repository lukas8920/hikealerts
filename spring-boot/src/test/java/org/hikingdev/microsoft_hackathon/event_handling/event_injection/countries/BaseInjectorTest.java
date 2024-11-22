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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BaseInjectorTest {
    protected IRawEventRepository iRawEventRepository;
    protected IEventRepository iEventRepository;
    protected ITrailRepository iTrailRepository;
    protected IRegionRepository iRegionRepository;

    protected LineString line;

    protected BaseInjector injector;
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

    protected abstract void mockTestThatMatchTrailsWorksForTrail(OpenAiEvent openAiEvent, RawEvent rawEvent, Trail trail);

    @Test
    public void testThatMatchTrailsWorksForTrail() throws ParseException {
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

        this.mockTestThatMatchTrailsWorksForTrail(openAiEvent, rawEvent, trail);

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
}

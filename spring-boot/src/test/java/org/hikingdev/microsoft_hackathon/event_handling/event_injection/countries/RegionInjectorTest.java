package org.hikingdev.microsoft_hackathon.event_handling.event_injection.countries;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class RegionInjectorTest extends BaseInjectorTest {
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
}

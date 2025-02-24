package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class EventTest {
    private Event event;
    private int year;

    @BeforeEach
    public void setup(){
        this.event = new Event();
        this.year = LocalDate.now().getYear();
    }

    @Test
    public void testTimeIntervalParsingWorks(){
        String fromDatetime = "31/12/YYYY 12:50:33";
        String toDatetime = "31/12/YYYY";

        event.parseTimeInterval(fromDatetime, toDatetime);

        LocalDateTime fromResult = event.getFromDatetime();
        LocalDateTime toResult = event.getToDatetime();

        assertThat(fromResult.getYear(), is(this.year));
        assertThat(toResult.getYear(), is(this.year));
    }

    @Test
    public void testTimeIntervalParsingNulls(){
        String fromDatetime = null;
        String toDatetime = "YYYY";

        event.parseTimeInterval(fromDatetime, toDatetime);

        assertThat(event.getFromDatetime(), nullValue());
        assertThat(event.getToDatetime(), nullValue());
    }

    @Test
    public void testTimeIntervalParsingNextYearInterval(){
        String fromDatetime = "31/12/YYYY 12:50:33";
        String toDatetime = "31/04/YYYY 15:48:20";

        event.parseTimeInterval(fromDatetime, toDatetime);

        LocalDateTime fromResult = event.getFromDatetime();
        LocalDateTime toResult = event.getToDatetime();

        assertThat(fromResult.getYear(), is(this.year));
        assertThat(toResult.getYear(), is((this.year + 1)));
    }

    @Test
    public void testTimeIntervalParsingNextYearMonth(){
        String fromDatetime = "01/01/YYYY 00:01:33";
        String toDatetime = "01/01/YYYY 00:02:20";

        event.parseTimeInterval(fromDatetime, toDatetime);

        LocalDateTime fromResult = event.getFromDatetime();
        LocalDateTime toResult = event.getToDatetime();

        assertThat(fromResult.getYear(), is((year + 1)));
    }

    @Test
    public void testTimeIntervalParsingFails(){
        String fromDatetime = "01/13/YYYY 00:01:33";
        String toDatetime = "01/01/YYYY 00:02:20";

        event.parseTimeInterval(fromDatetime, toDatetime);

        assertThat(event.getFromDatetime(), nullValue());
        assertThat(event.getToDatetime().getYear(), is((year + 1)));
    }

    @Test
    public void testMidPointOfLinestring() throws ParseException {
        WKBWriter wkbWriter = new WKBWriter();
        Trail trail = new Trail();
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 1), new Coordinate(3, 1), new Coordinate(9, 1)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LineString lineString = new LineString(coordinateSequence, geometryFactory);
        trail.setCoordinates(wkbWriter.write(lineString));

        event.calculateMidCoordinate(trail);

        assertThat(event.getMidLatitudeCoordinate(), is(1.0));
        assertThat(event.getMidLongitudeCoordinate(), is(5.0));
    }

    @Test
    public void testThatTimeParsingEvalOriginalEvent(){
        LocalDateTime test = LocalDateTime.now();
        event.setFromDatetime(test);
        event.setToDatetime(test);

        String fromDatetime = null;
        String toDatetime = "01/01/YYYY 00:02:20";

        event.parseTimeInterval(fromDatetime, toDatetime);

        assertThat(event.getFromDatetime(), is(test));
        assertThat(event.getToDatetime().getYear(), is((year + 1)));
    }

    @Test
    public void testNullFromDatetime(){
        LocalDateTime test = LocalDateTime.now();
        event.setToDatetime(test);

        String fromDatetime = null;
        String toDatetime = "01/01/2024 00:02:20";

        event.parseTimeInterval(fromDatetime, toDatetime);

        assertThat(event.getFromDatetime(), nullValue());
        assertThat(event.getToDatetime().getYear(), is((2024)));
    }

    @Test
    public void testEmptyToDatetime(){
        String fromDatetime = "04/11/2024";
        String toDatetime = "";

        event.parseTimeInterval(fromDatetime, toDatetime);

        assertThat(event.getFromDatetime().getYear(), is(2024));
        assertThat(event.getToDatetime(), nullValue());
    }

    @Test
    public void testOtherPlaceholder(){
        String fromDatetime = "dd/mm/YYYY";
        String toDatetime = "dd/mm/YYYY";

        event.parseTimeInterval(fromDatetime, toDatetime);

        assertThat(event.getFromDatetime(), nullValue());
        assertThat(event.getToDatetime(), nullValue());
    }
}

package org.devbros.microsoft_hackathon.map_layer;

import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.devbros.microsoft_hackathon.map_layer.MapLayerService;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKBWriter;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapLayerServiceTest {
    @Test
    @Disabled
    //unreliable during build process
    public void testMapLayerProcedure(){
        GeometryFactory geometryFactory = new GeometryFactory();
        WKBWriter wkbWriter = new WKBWriter();
        Coordinate[] coordinates = new Coordinate[]{new Coordinate(1, 1), new Coordinate(2, 2)};
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LineString line = new LineString(coordinateSequence, geometryFactory);

        Trail trail1 = new Trail();
        trail1.setTrailId("55555L");
        trail1.setCoordinates(wkbWriter.write(line));
        trail1.setUnitcode("abc");
        trail1.setCountry("ZZ");

        ITrailRepository trailRepository = mock(ITrailRepository.class);
        MapLayerService mapLayerService = new MapLayerService(trailRepository);

        when(trailRepository.fetchTrails(0, 1000)).thenReturn(new ArrayList<>());
        when(trailRepository.fetchTrails(1000, 1000)).thenReturn(null);

        new Thread(mapLayerService::updateGeoJsonFile).start();

        File file = new File("src/main/resources/static/layer.geojson");

        assertThat(file.exists(), is(true));
        file.delete();
    }
}

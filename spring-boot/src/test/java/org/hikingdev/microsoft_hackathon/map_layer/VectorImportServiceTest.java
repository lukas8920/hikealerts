package org.hikingdev.microsoft_hackathon.map_layer;

import org.hikingdev.microsoft_hackathon.map_layer.entities.TileHandler;
import org.hikingdev.microsoft_hackathon.map_layer.entities.TileWithCoords;
import org.hikingdev.microsoft_hackathon.map_layer.entities.TileWithoutCoords;
import org.hikingdev.microsoft_hackathon.repository.tiles.ITileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class VectorImportServiceTest {
    private VectorImportService vectorImportService;

    @BeforeEach
    public void setup(){
        ITileRepository iTileRepository = mock(ITileRepository.class);
        this.vectorImportService = spy(new VectorImportService(iTileRepository));
    }

    @Test
    public void testThatHandleTilePersistsEmptyTile() throws InterruptedException {
        vectorImportService.zoom = "0";
        TileHandler tile = new TileWithoutCoords("0", "0", null);

        this.vectorImportService.handleTile(tile);

        assertThat(vectorImportService.removalBuffer.contains(tile), is(true));
    }

    @Test
    public void testThatHandleTilePersistsTileWithData() throws InterruptedException {
        vectorImportService.zoom = "0";
        TileHandler tile = new TileWithCoords("0", "1", new byte[]{});

        this.vectorImportService.handleTile(tile);

        assertThat(vectorImportService.tileBuffer.contains(tile), is(true));
    }

    @Test
    public void testThatHandleTileHandlesZoomChange() throws InterruptedException {
        vectorImportService.zoom = "0";
        TileHandler tileHandler = mock(TileHandler.class);
        vectorImportService.tileBuffer.add(tileHandler);
        vectorImportService.removalBuffer.add(tileHandler);
        TileHandler tile = new TileWithCoords("1", "1", new byte[]{});

        this.vectorImportService.handleTile(tile);

        assertThat(vectorImportService.tileBuffer.size(), is(1));
        assertThat(vectorImportService.removalBuffer.isEmpty(), is(true));
        assertThat(vectorImportService.tileBuffer.contains(tile), is(true));
        assertThat(vectorImportService.zoom, is("1"));
    }

    @Test
    public void testThatHandleTileHandlesNullTile() throws InterruptedException {
        doReturn(null).doThrow(new RuntimeException()).when(this.vectorImportService).blockQueue();
        vectorImportService.zoom = "0";
        TileHandler tile = null;
        TileHandler tileHandler = mock(TileHandler.class);
        vectorImportService.tileBuffer.add(tileHandler);
        vectorImportService.removalBuffer.add(tileHandler);

        this.vectorImportService.handleTile(tile);

        assertThat(vectorImportService.tileBuffer.size(), is(0));
        assertThat(vectorImportService.removalBuffer.size(), is(0));
        assertThat(vectorImportService.zoom, is("0"));
    }
}

package org.hikingdev.microsoft_hackathon.repository.tiles;

import org.hikingdev.microsoft_hackathon.map_layer.entities.TileHandler;

import java.util.List;

public interface ITileRepository {
    void save(TileHandler tile);
    void save(List<TileHandler> tiles, String zoom);
    byte[] query(int z, int x, int y);
    void remove(String zoom, String keyTile);
    void remove(List<TileHandler> tiles, String zoom);
}

package org.hikingdev.microsoft_hackathon.repository.tiles;

import org.hikingdev.microsoft_hackathon.map_layer.entities.Tile;

import java.util.List;

public interface ITileRepository {
    void save(Tile tile);
    void save(List<Tile> tiles);
    byte[] query(int z, int x, int y);
    void remove(String zoom, String keyTile);
    void remove(List<Tile> tiles);
}

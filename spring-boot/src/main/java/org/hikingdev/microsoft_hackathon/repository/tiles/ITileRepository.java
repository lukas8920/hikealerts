package org.hikingdev.microsoft_hackathon.repository.tiles;

import org.hikingdev.microsoft_hackathon.map_layer.Tile;

public interface ITileRepository {
    void save(Tile tile);
    byte[] query(int z, int x, int y);
}

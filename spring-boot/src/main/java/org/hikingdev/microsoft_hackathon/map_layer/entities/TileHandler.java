package org.hikingdev.microsoft_hackathon.map_layer.entities;

import org.hikingdev.microsoft_hackathon.repository.tiles.ITileRepository;

public interface TileHandler {
    void persist(ITileRepository iTileRepository);
    String getZoom();
    String getTileKey();
    byte[] getTile();
}

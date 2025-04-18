package org.hikingdev.microsoft_hackathon.map_layer.entities;

import org.hikingdev.microsoft_hackathon.repository.tiles.ITileRepository;

public class TileWithCoords extends Tile implements TileHandler {
    public TileWithCoords(String zoom, String tileKey, byte[] tile) {
        super(zoom, tileKey, tile);
    }

    @Override
    public void persist(ITileRepository iTileRepository) {
        iTileRepository.save(this);
    }
}

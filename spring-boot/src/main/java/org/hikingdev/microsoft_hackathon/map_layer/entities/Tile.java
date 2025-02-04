package org.hikingdev.microsoft_hackathon.map_layer.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Tile {
    private String zoom;
    private String tileKey;
    private byte[] tile;
}

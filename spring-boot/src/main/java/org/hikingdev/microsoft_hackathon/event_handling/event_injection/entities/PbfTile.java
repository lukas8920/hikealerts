package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PbfTile {
    private int x;
    private int y;
    private int z;

    @Override
    public boolean equals(Object object){
        PbfTile pbfTile = (PbfTile) object;
        return pbfTile.getX() == this.x && pbfTile.getY() == this.y && pbfTile.getZ() == z;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}

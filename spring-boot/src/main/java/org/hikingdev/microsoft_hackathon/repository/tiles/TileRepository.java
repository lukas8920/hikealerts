package org.hikingdev.microsoft_hackathon.repository.tiles;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import org.hikingdev.microsoft_hackathon.map_layer.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TileRepository implements ITileRepository {
    private static final Logger logger = LoggerFactory.getLogger(TileRepository.class);
    private static final byte[] emptyTile = VectorTile.Tile.newBuilder().build().toByteArray();

    private final RedisTemplate<String, byte[]> tileRepository;

    @Autowired
    public TileRepository(RedisTemplate<String, byte[]> tileRepository){
        this.tileRepository = tileRepository;
    }

    @Override
    public void save(Tile tile) {
        try {
            this.tileRepository.opsForHash().put(tile.getZoom(), tile.getTileKey(), tile.getTile());
            this.tileRepository.expire(tile.getZoom(), Duration.ofHours(48));
        } catch (Exception e){
            logger.error("Error while saving.", e);
        }
    }

    @Override
    public byte[] query(int z, int x, int y) {
        String zoom = "zoom_" + z;
        String tileKey = "tile:" + z + ":" + x + ":" + y;

        // Check Redis cache first
        byte[] cachedTile = (byte[]) this.tileRepository.opsForHash().get(zoom, tileKey);
        if (cachedTile != null) {
            return cachedTile;
        }

        // Return empty tile if not in cache
        return emptyTile;
    }
}

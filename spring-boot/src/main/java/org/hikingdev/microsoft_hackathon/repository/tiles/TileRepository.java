package org.hikingdev.microsoft_hackathon.repository.tiles;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import org.hikingdev.microsoft_hackathon.map_layer.entities.Tile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void save(List<Tile> tiles) {
        String zoom = tiles.get(0).getZoom();
        Map<String, byte[]> values = tiles.stream().collect(Collectors.toMap(Tile::getTileKey, Tile::getTile));
        try {
            this.tileRepository.opsForHash().putAll(zoom, values);
        } catch (Exception e){
            logger.error("Error while saving. ", e);
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

    @Override
    public void remove(String zoom, String keyTile) {
        try {
            this.tileRepository.opsForHash().delete(zoom, keyTile);
        } catch (Exception e){
            logger.error("Error while deleting.", e);
        }
    }

    @Override
    public void remove(List<Tile> tiles) {
        String zoom = tiles.get(0).getZoom();
        List<String> keyTiles = tiles.stream().map(Tile::getTileKey).toList();
        try {
            this.tileRepository.opsForHash().delete(zoom, keyTiles);
        } catch (Exception e){
            logger.error("Error while saving. ", e);
        }
    }
}

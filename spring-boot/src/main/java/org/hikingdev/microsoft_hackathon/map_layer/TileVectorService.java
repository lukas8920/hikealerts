package org.hikingdev.microsoft_hackathon.map_layer;

import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.repository.tiles.ITileRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.BaseScheduler;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class TileVectorService extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TileVectorService.class);

    private static final Object lock = new Object();

    public static final int MIN_ZOOM = 7;
    public static final int MAX_ZOOM = 13;

    private final ScheduledExecutorService executorService;
    private final ITrailRepository iTrailRepository;
    private final ITileRepository iTileRepository;

    private boolean interrupted_flag = false;

    @Autowired
    public TileVectorService(ITrailRepository iTrailRepository, ITileRepository iTileRepository){
        this.iTrailRepository = iTrailRepository;
        this.executorService = Executors.newScheduledThreadPool(10);
        this.iTileRepository = iTileRepository;
    }

    public byte[] query(int z, int x, int y) throws BadRequestException {
        // Validate zoom level
        if (z < MIN_ZOOM || z > MAX_ZOOM) {
            throw new BadRequestException("Invalid zoom level");
        }

        // Validate tile coordinates
        int maxTiles = (int) Math.pow(2, z);
        if (x < 0 || x >= maxTiles || y < 0 || y >= maxTiles) {
            throw new BadRequestException("Invalid tile coordinates.");
        }
        return this.iTileRepository.query(z, x, y);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void runProcedure() {
        while (running && !Thread.currentThread().isInterrupted()){
            long start_timestamp = System.currentTimeMillis();

            TileGenerator tileGenerator = this.getTileGenerator();

            for (int zoomLevel = MIN_ZOOM; zoomLevel <= MAX_ZOOM; zoomLevel++) {
                logger.info("Start refreshing zoomLevel {}", zoomLevel);
                // Get the tile ranges for the current zoom level
                int maxTileIndex = (int) Math.pow(2, zoomLevel); // Number of tiles in one direction at this zoom level
                for (int x = 0; x < maxTileIndex; x++) {
                    for (int y = 0; y < maxTileIndex; y++) {
                        // Call your tile generation method here
                        int finalZoomLevel = zoomLevel;
                        int finalY = y;
                        int finalX = x;

                        this.executorService.submit(() -> {
                            try {
                                executorService.schedule(() -> {
                                    if (!interrupted_flag){
                                        try {
                                            this.generateTile(tileGenerator, finalX, finalY, finalZoomLevel);
                                        } catch (Exception e){
                                            logger.error("Error while parsing {}, {}, {}. But resume...", finalZoomLevel, finalX, finalY, e);
                                        }
                                    }}, 20, TimeUnit.SECONDS);
                            } catch (Exception timeout){
                                logger.warn("Tile Vector timeout for {}, {}, {}", finalZoomLevel, finalX, finalY);
                            }});
                    }
                }
                logger.info("Next zoom level");
            }

            try {
                long end_timestamp = System.currentTimeMillis();
                long diff = (end_timestamp - start_timestamp) / 60000;
                logger.info("TileVectorService finished refreshing the cache and goes to sleep - {} minutes", diff);
                TimeUnit.HOURS.sleep(24);
            } catch (InterruptedException e){
                logger.info("TileVectorService has been interrupted.");
                interrupted_flag = true;
                break;
            }
        }
    }

    public static Object lock(){
        return lock;
    }

    public void generateTile(TileGenerator tileGenerator, int x, int y, int z) {
        Optional<byte[]> tile = tileGenerator.generateTile(x, y, z);
        // cache tile
        if (tile.isPresent()){
            String zoom = "zoom_" + z;
            String tileKey = "tile:" + z + ":" + x + ":" + y;
            Tile outputTile = new Tile(zoom, tileKey, tile.get());
            this.iTileRepository.save(outputTile);
        }
    }

    public TileGenerator getTileGenerator(){
        List<Trail> trails = this.iTrailRepository.fetchTrails(0, 50000000);
        WKBReader wkbReader = new WKBReader();
        List<SpatialItem> lineStrings = trails.stream().map(t -> {
            try {
                return new SpatialItem(t.getId(), t.getTrailname(), wkbReader.read(t.getCoordinates()));
            } catch (ParseException e) {
                logger.error("Error while parsing {}", t.getTrailname());
                return null;
            }
        }).collect(Collectors.toList());
        trails.clear();
        logger.info("Fetched {} trails", lineStrings.size());
        return new TileGenerator(lineStrings);
    }
}

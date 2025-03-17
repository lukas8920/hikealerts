package org.hikingdev.microsoft_hackathon.map_layer;

import org.hikingdev.microsoft_hackathon.map_layer.entities.TileHandler;
import org.hikingdev.microsoft_hackathon.repository.tiles.ITileRepository;
import org.hikingdev.microsoft_hackathon.util.BaseScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class VectorImportService extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(VectorImportService.class);

    // queue size is the sum tile buffer and removal buffer threshold for persisting to db
    private final BlockingQueue<TileHandler> tileQueue = new ArrayBlockingQueue<>(200000);

    final IPersist<TileHandler> tileBuffer = new TileBuffer();
    final IPersist<TileHandler> removalBuffer = new RemovalBuffer();

    private final ITileRepository iTileRepository;

    int timeout = 5;
    String zoom = "intital";

    @Autowired
    public VectorImportService(ITileRepository iTileRepository){
        this.iTileRepository = iTileRepository;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public void addToQueue(TileHandler tile) throws InterruptedException {
        this.tileQueue.put(tile);
    }

    @Override
    protected void runProcedure() {
        while (running && !Thread.currentThread().isInterrupted()){
            TileHandler tile = tileQueue.poll();

            try {
                this.handleTile(tile);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Exit the loop if interrupted
            }
        }
    }

    public void handleTile(TileHandler tile) throws InterruptedException {
        if (tile != null && zoom.equals(tile.getZoom())){
            // reset timeout incrementer
            timeout = 5;

            if (tile.getTile() == null){
                this.removalBuffer.add(tile);
                if (this.removalBuffer.size() > 100000){
                    this.removalBuffer.persist(zoom);
                    this.removalBuffer.clear();
                }
            } else {
                this.tileBuffer.add(tile);
                if (this.tileBuffer.size() > 500){
                    this.tileBuffer.persist(zoom);
                    this.tileBuffer.clear();
                }
            }
        } else {
            boolean hasChangedZoomLevel = (tile != null && !zoom.equals(tile.getZoom()));
            logger.debug("Next zoom level in tile handler: " + hasChangedZoomLevel);

            // No message found, clear buffers and go to sleep
            if (hasChangedZoomLevel || (timeout == 300 && tileBuffer.size() > 0)){
                this.tileBuffer.persist(zoom);
                this.tileBuffer.clear();
            }
            if (hasChangedZoomLevel || (timeout == 300 && removalBuffer.size() > 0)){
                this.removalBuffer.persist(zoom);
                this.removalBuffer.clear();
            }
            // In case of changed zoom level process next tile
            if (hasChangedZoomLevel){
                if (tile.getTile() == null){
                    this.removalBuffer.add(tile);
                } else {
                    this.tileBuffer.add(tile);
                }
                logger.debug("Update to next zoom level: " + tile.getZoom());
                zoom = tile.getZoom();
            }

            if (!hasChangedZoomLevel){
                try {
                    this.sleep();
                } catch (InterruptedException e) {
                    throw new InterruptedException();
                }

                timeout = timeout == 5 ? 10 : 300;
            }
        }
    }

    void sleep() throws InterruptedException {
        logger.debug("Sleeping VectorImportService for {} seconds", timeout);
        TimeUnit.SECONDS.sleep(timeout);
    }

    class TileBuffer extends ArrayList<TileHandler> implements IPersist<TileHandler> {
        @Override
        public void persist(String zoom) {
            logger.debug("save tiles with data: " + zoom + " - no. of items: " + this.size());
            VectorImportService.this.iTileRepository.save(this, zoom);
        }
    }

    class RemovalBuffer extends ArrayList<TileHandler> implements IPersist<TileHandler> {
        @Override
        public void persist(String zoom) {
            VectorImportService.this.iTileRepository.remove(this, zoom);
        }
    }

    interface IPersist<T> extends List<T> {
        void persist(String zoom);
    }
}

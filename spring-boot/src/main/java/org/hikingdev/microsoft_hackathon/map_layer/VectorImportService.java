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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class VectorImportService extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(VectorImportService.class);

    // queue size is the sum tile buffer and removal buffer threshold for persisting to db
    private final Queue<TileHandler> tileQueue = new ArrayBlockingQueue<>(200000);
    private final IPersist<TileHandler> tileBuffer = new TileBuffer();
    private final IPersist<TileHandler> removalBuffer = new RemovalBuffer();

    private final ITileRepository iTileRepository;

    @Autowired
    public VectorImportService(ITileRepository iTileRepository){
        this.iTileRepository = iTileRepository;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    public void addToQueue(TileHandler tile){
        this.tileQueue.offer(tile);
    }

    @Override
    protected void runProcedure() {
        int timeout = 5;

        while (running && !Thread.currentThread().isInterrupted()){
            TileHandler tile = tileQueue.poll();
            if (tile != null){
                // reset timeout incrementer
                timeout = 5;

                if (tile.getTile() == null){
                    this.removalBuffer.add(tile);
                    if (this.removalBuffer.size() > 100000){
                        this.removalBuffer.persist();
                        this.removalBuffer.clear();
                    }
                } else {
                    this.tileBuffer.add(tile);
                    if (this.tileBuffer.size() > 500){
                        this.tileBuffer.persist();
                        this.tileBuffer.clear();
                    }
                }
            } else {
                // No message found, clear buffers and go to sleep
                if (timeout == 300 && tileBuffer.size() > 0){
                    this.tileBuffer.persist();
                    this.tileBuffer.clear();
                }
                if (timeout == 300 && removalBuffer.size() > 0){
                    this.removalBuffer.persist();
                    this.removalBuffer.clear();
                }

                try {
                    TimeUnit.SECONDS.sleep(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Exit the loop if interrupted
                }

                timeout = timeout == 5 ? 10 : 300;
            }
        }
    }

    class TileBuffer extends ArrayList<TileHandler> implements IPersist<TileHandler> {
        @Override
        public void persist() {
            VectorImportService.this.iTileRepository.save(this);
        }
    }

    class RemovalBuffer extends ArrayList<TileHandler> implements IPersist<TileHandler> {
        @Override
        public void persist() {
            VectorImportService.this.iTileRepository.remove(this);
        }
    }

    interface IPersist<T> extends List<T> {
        void persist();
    }
}

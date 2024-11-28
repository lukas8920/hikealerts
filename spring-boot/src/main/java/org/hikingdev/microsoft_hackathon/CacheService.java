package org.hikingdev.microsoft_hackathon;


import org.hikingdev.microsoft_hackathon.event_handling.event_injection.IEventInjection;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.PbfTile;
import org.hikingdev.microsoft_hackathon.map_layer.MapLayerService;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.util.BaseScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final IEventRepository iEventRepository;
    private final MapLayerService mapLayerService;

    @Autowired
    public CacheService(IEventRepository iEventRepository, MapLayerService mapLayerService){
        this.iEventRepository = iEventRepository;
        this.mapLayerService = mapLayerService;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void runProcedure() {
        while (running && !Thread.currentThread().isInterrupted()){
            // block thread otherwise
            this.iEventRepository.refreshCache();
            // trail cache will be refreshed with other frequency
            this.mapLayerService.requestGeoJsonFileUpdate();
            try {
                logger.info("Cache refreshing sleeps for one hour.");
                TimeUnit.HOURS.sleep(1);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }
}

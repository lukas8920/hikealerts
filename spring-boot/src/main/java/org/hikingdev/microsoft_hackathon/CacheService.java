package org.hikingdev.microsoft_hackathon;


import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.util.BaseScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService extends BaseScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private final IEventRepository iEventRepository;

    @Autowired
    public CacheService(IEventRepository iEventRepository){
        this.iEventRepository = iEventRepository;
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
            try {
                logger.info("Cache refreshing sleeps for one hour.");
                TimeUnit.HOURS.sleep(1);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
    }
}

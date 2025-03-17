package org.hikingdev.microsoft_hackathon.util.threading;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class BaseScheduler {
    protected ExecutorService executorService;
    protected volatile boolean running = true;

    @PostConstruct
    public void init() {
        // Start the queue listener in a separate thread
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::runProcedure);
    }

    protected abstract Logger getLogger();

    protected abstract void runProcedure();

    @PreDestroy
    public void shutdown() {
        running = false; // Signal the listener to stop running
        executorService.shutdown(); // Initiate an orderly shutdown of the ExecutorService

        try {
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow(); // Forcefully shut down if tasks don't finish in time
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow(); // Handle any interruption during shutdown
            Thread.currentThread().interrupt();
        }

        getLogger().info("Queue listener service has been gracefully shut down.");
    }
}

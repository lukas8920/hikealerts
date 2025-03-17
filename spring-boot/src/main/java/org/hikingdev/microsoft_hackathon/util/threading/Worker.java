package org.hikingdev.microsoft_hackathon.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Phaser;

public class Worker implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class.getName());

    private final Phaser phaser;
    private final Runnable runnable;

    public Worker(Phaser phaser, Runnable runnable) {
        this.phaser = phaser;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            logger.debug("Worker " + Thread.currentThread().getId() + " is working...");
            runnable.run();
            logger.debug("Worker " + Thread.currentThread().getId() + " has finished.");
        } finally {
            phaser.arriveAndDeregister();  // Indicate that the thread has finished
        }
    }
}

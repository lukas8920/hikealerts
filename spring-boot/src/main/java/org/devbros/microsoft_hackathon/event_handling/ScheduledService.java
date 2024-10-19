package org.devbros.microsoft_hackathon.event_handling;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledService {
    protected final QueueClient queueClient;
    protected final ObjectMapper objectMapper;

    protected ExecutorService executorService;
    protected volatile boolean running = true;

    public ScheduledService(QueueClient queueClient){
        this.queueClient = queueClient;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        // Start the queue listener in a separate thread
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::listenToQueue);
    }

    protected abstract Logger getLogger();

    protected abstract void processMessage(String messageBody);

    private void listenToQueue() {
        while (running && !Thread.currentThread().isInterrupted()) {
            QueueMessageItem message = queueClient.receiveMessage();
            if (message != null) {
                // Process the message
                String messageBody = message.getBody().toString();
                messageBody = messageBody.replace("None", "null");
                getLogger().info("Received message: " + messageBody);

                processMessage(messageBody);

                // Delete the message from the queue after processing
                queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
            } else {
                // No message found, sleep for 5 minutes
                try {
                    getLogger().info("Go to sleep as no further messages.");
                    TimeUnit.MINUTES.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break; // Exit the loop if interrupted
                }
            }
        }
    }

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

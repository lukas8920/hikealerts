package org.hikingdev.microsoft_hackathon.util;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class ScheduledService extends BaseScheduler {
    protected final QueueClient queueClient;
    protected final ObjectMapper objectMapper;

    public ScheduledService(QueueClient queueClient){
        this.queueClient = queueClient;
        this.objectMapper = new ObjectMapper();
    }

    protected abstract Logger getLogger();

    protected abstract void processMessage(String messageBody);

    @Override
    protected void runProcedure(){
        while (running && !Thread.currentThread().isInterrupted()) {
            getLogger().info("Query next message.");
            QueueMessageItem message = queueClient.receiveMessage();
            if (message != null) {
                getLogger().info("Received Message with id: " + message.getMessageId());
                // Process the message
                String messageBody = message.getBody().toString();
                messageBody = messageBody.replace("None", "null");

                processMessage(messageBody);

                // Delete the message from the queue after processing
                getLogger().info("Delete message with id: " + message.getMessageId());
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
}

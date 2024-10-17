package org.devbros.microsoft_hackathon.event_injection;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.devbros.microsoft_hackathon.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_injection.entities.OpenAiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class EventListenerService {
    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class.getName());

    private final IEventInjection iEventInjection;
    private final QueueClient queueClient;
    private final ObjectMapper objectMapper;

    private ExecutorService executorService;
    private volatile boolean running = true;

    @Autowired
    public EventListenerService(IEventInjection iEventInjection, @Qualifier("queueConnectionString") String queueConnectionString) {
        this.iEventInjection = iEventInjection;
        this.objectMapper = new ObjectMapper();

        this.queueClient = new QueueClientBuilder()
                .connectionString(queueConnectionString)
                .queueName("openai-events")
                .buildClient();
    }

    @PostConstruct
    public void init() {
        // Start the queue listener in a separate thread
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(this::listenToQueue);
    }

    private void listenToQueue() {
        while (running && !Thread.currentThread().isInterrupted()) {
            QueueMessageItem message = queueClient.receiveMessage();
            if (message != null) {
                // Process the message
                String messageBody = message.getBody().toString();
                messageBody = messageBody.replace("None", "null");
                logger.info("Received message: " + messageBody);

                List<OpenAiEvent> openAiEvents;
                try {
                    openAiEvents = this.objectMapper.readValue(messageBody, this.objectMapper.getTypeFactory().constructCollectionType(List.class, OpenAiEvent.class));
                    logger.info("inject events into database.");
                    List<Message> responses = this.iEventInjection.injectEvent(openAiEvents);
                    if (!responses.isEmpty() && !responses.get(0).getId().equals("0")){
                        logger.warn("Some events were not added to the database: " + responses.toString());
                    }
                } catch (JsonProcessingException e) {
                    logger.error("Could not json parse: " + messageBody);
                }

                // Delete the message from the queue after processing
                queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
            } else {
                // No message found, sleep for 5 minutes
                try {
                    logger.info("Go to sleep as no further messages.");
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

        logger.info("Queue listener service has been gracefully shut down.");
    }
}

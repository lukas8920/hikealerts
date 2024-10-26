package org.devbros.microsoft_hackathon.event_handling;

import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.devbros.microsoft_hackathon.event_handling.event_injection.IEventInjection;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.devbros.microsoft_hackathon.util.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventListenerService extends ScheduledService {
    private final Logger logger;

    private final IEventInjection iEventInjection;

    @Autowired
    public EventListenerService(IEventInjection iEventInjection, @Qualifier("queueConnectionString") String queueConnectionString) {
        super(new QueueClientBuilder()
                .connectionString(queueConnectionString)
                .queueName("openai-events")
                .buildClient());

        this.logger = LoggerFactory.getLogger(EventListenerService.class.getName());

        this.iEventInjection = iEventInjection;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void processMessage(String messageBody) {
        List<OpenAiEvent> openAiEvents;
        try {
            openAiEvents = this.objectMapper.readValue(messageBody, this.objectMapper.getTypeFactory().constructCollectionType(List.class, OpenAiEvent.class));
            getLogger().info("inject events into database.");
            List<Message> responses = this.iEventInjection.injectEvent(openAiEvents);
            if (!responses.isEmpty() && !responses.get(0).getId().equals("0")){
                getLogger().warn("Some events were not added to the database: " + responses.toString());
            }
        } catch (JsonProcessingException e) {
            getLogger().error("Could not json parse: " + messageBody);
        } catch (Exception e){
            getLogger().error("Could not inject event: " + messageBody, e);
        }
    }
}

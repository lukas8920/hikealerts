package org.devbros.microsoft_hackathon.event_handling;

import com.azure.storage.queue.QueueClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devbros.microsoft_hackathon.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.util.ScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RemovalService extends ScheduledService {
    private final Logger logger;

    private final IEventRepository iEventRepository;

    @Autowired
    public RemovalService(IEventRepository iEventRepository, @Qualifier("queueConnectionString") String queueConnectionString) {
        super(new QueueClientBuilder()
                .connectionString(queueConnectionString)
                .queueName("deleted-events")
                .buildClient());

        this.logger = LoggerFactory.getLogger(EventListenerService.class.getName());

        this.iEventRepository = iEventRepository;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected void processMessage(String messageBody) {
        RemovalEntity removalEntity;
        try {
            removalEntity = this.objectMapper.readValue(messageBody, RemovalEntity.class);
            getLogger().info("check event deletion from database.");
            this.iEventRepository.deleteEventsNotInList(removalEntity.getIds(), removalEntity.getCountry());
        } catch (JsonProcessingException e) {
            getLogger().error("Could not json parse: " + messageBody);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class RemovalEntity {
        private String country;
        private List<String> ids;
    }
}

package org.hikingdev.microsoft_hackathon.event_handling.event_injection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.util.AiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

    private static final String openaiInstruction = "For each alert identify most likely matching trails with problems based on country, area and trail information provided with the alert. \n Do not output and ignore alerts where: \n - no trail name identification is possible and the problem does not seem to affect trails in the entire park or region. \n - no trail name, no park name or no region can be identified. \n - there seems to be no problem with trail conditions. \n For the remaining alerts, output the results as json list. Each json item consists of: Event id, country, park_name, region, trail_name, from_date and to_date \n Each item must have either a trail name and a park name and/or a region. Trail names might be in the alert description. The description should not be in the response. \n Each item might have a from and to date in format dd/mm/YYYY. If no year mentioned, enter a placeholder YYYY. \n One alert can have zero, one or multiple json items - each trail should go in a separate json item.  \n Replace any known abbreviations and correct known misspellings. \n Respond only with the data without any additional comments.";

    private final String endpoint;
    private final String api_key;

    @Autowired
    public OpenAiService(@Qualifier("openai_key") String api_key, @Qualifier("openai_endpoint") String endpoint){
        this.api_key = api_key;
        this.endpoint = endpoint;
    }

    public OpenAiEvent sendOpenAiRequest(String jsonString) throws AiException {
        try {
            // Create the message structure
            Map<String, Object> messageDict = new HashMap<>();
            messageDict.put("model", "gpt-4");

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "system", "content", openaiInstruction),
                    Map.of("role", "user", "content", jsonString)
            );

            messageDict.put("messages", messages);

            ObjectMapper mapper = new ObjectMapper();
            String requestBody = mapper.writeValueAsString(messageDict).replace("\\", "");
            logger.info("Send {} to openai service.", requestBody);

            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Create HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .header("Content-Type", "application/json")
                    .header("api-key", api_key)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // Send request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Received {} from openai service.", response.body());

            return readValue(response.body());
        } catch (Exception e) {
            logger.error("No valid response for event input from openai service ", e);
            throw new AiException("AI does not generate a valid response for user input.");
        }
    }

    private OpenAiEvent readValue(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // Parse the JSON string into a JsonNode
        JsonNode rootNode = mapper.readTree(body);

        // Navigate to "content" inside "message"
        String content = rootNode.path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        return mapper.readValue(body, OpenAiEvent.class);
    }
}

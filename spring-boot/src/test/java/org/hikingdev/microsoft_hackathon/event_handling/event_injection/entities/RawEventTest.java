package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RawEventTest {
    @Test
    public void testThatParseToJsonWorks() throws JsonProcessingException {
        RawEvent rawEvent = new RawEvent("NZ", "dummyTitle", "dummyDescription", LocalDate.now(), LocalDate.now());

        String jsonString = rawEvent.parseToJson();

        JsonNode actualJsonNode = new ObjectMapper().readTree(jsonString);

        assertThat(actualJsonNode.get("country").asText(), is("NZ"));
        assertThat(actualJsonNode.get("alerts").elements().next().get("title").asText(), is("dummyTitle"));
        assertThat(actualJsonNode.get("alerts").elements().next().get("description").asText(), is("dummyDescription"));
        assertThat(actualJsonNode.get("alerts").elements().next().get("event_id").asText(), is(rawEvent.getEventId()));
    }
}

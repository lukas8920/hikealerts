package org.devbros.microsoft_hackathon.event_handling;

import com.google.gson.Gson;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

public class MapEventSerializer implements RedisSerializer<MapEvent> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(MapEvent mapEvent) throws SerializationException {
        return gson.toJson(mapEvent).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public MapEvent deserialize(byte[] bytes) throws SerializationException {
        String tmpString = new String(bytes, StandardCharsets.UTF_8);
        return gson.fromJson(tmpString, MapEvent.class);
    }
}

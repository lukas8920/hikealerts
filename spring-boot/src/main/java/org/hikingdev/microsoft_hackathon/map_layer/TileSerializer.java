package org.hikingdev.microsoft_hackathon.map_layer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class TileSerializer implements RedisSerializer<byte[]> {
    @Override
    public byte[] serialize(byte[] value) throws SerializationException {
        return value;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws SerializationException {
        return bytes;
    }
}

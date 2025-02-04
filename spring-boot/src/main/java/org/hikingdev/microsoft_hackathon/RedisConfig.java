package org.hikingdev.microsoft_hackathon;

import org.hikingdev.microsoft_hackathon.event_handling.MapEventSerializer;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.hikingdev.microsoft_hackathon.map_layer.TileSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.embedded.RedisServer;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Bean
    @Profile({"test", "mock"})
    public RedisConnectionFactory lettuceTestConnectionFactory(RedisServer redisServer) {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, 6379));
    }

    @Bean
    @Profile("prod")
    public RedisConnectionFactory lettuceProdConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, 6379));
    }

    @Bean
    public RedisTemplate<String, MapEvent> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, MapEvent> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new MapEventSerializer());
        return template;
    }

    @Bean(name = "chatTemplate")
    public RedisTemplate<String, String> chatTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, byte[]> byteArrayRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer()); // Key serializer (e.g., String)
        template.setValueSerializer(new TileSerializer()); // Value serializer for byte arrays
        return template;
    }
}
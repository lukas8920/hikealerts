package org.hikingdev.microsoft_hackathon;

import org.hikingdev.microsoft_hackathon.event_handling.MapEventSerializer;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.MapEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
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
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost, 6379));
        jedisConnectionFactory.setTimeout(5000);
        return jedisConnectionFactory;
    }

    @Bean
    @Profile("prod")
    public RedisConnectionFactory lettuceProdConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(new RedisStandaloneConfiguration(redisHost, 6379));
        jedisConnectionFactory.setTimeout(5000);
        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, MapEvent> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, MapEvent> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new MapEventSerializer());
        return template;
    }
}
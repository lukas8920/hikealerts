package org.devbros.microsoft_hackathon;

import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Configuration
@Profile("test")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Bean
    public RedisServer redisServer() throws Exception {
        redisServer = new RedisServer(6379); // Specify the port you want
        redisServer.start();
        return redisServer;
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}

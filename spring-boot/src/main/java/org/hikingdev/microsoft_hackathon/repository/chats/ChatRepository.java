package org.hikingdev.microsoft_hackathon.repository.chats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ChatRepository implements IChatRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChatRepository.class);
    private static final String FIELD_PREFIX = "user_";

    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public ChatRepository(@Qualifier("chatTemplate") RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(String message, Long userId) {
        try {
            this.redisTemplate.opsForValue().set(FIELD_PREFIX + userId, message, 10, TimeUnit.MINUTES);
        } catch (Exception e){
            logger.error("Error while caching chat for user {}", userId);
        }
    }

    @Override
    public String findMessage(Long userid) {
        try {
            return this.redisTemplate.opsForValue().get(FIELD_PREFIX + userid);
        } catch (Exception e){
            logger.error("Error while trying to get chat for {}", userid);
            return null;
        }
    }

    @Override
    public void clear(Long userId) {
        try {
            this.redisTemplate.delete(FIELD_PREFIX + userId);
        } catch (Exception e){
            logger.error("Error while trying to delete chat for {}", userId);
        }
    }
}

package org.hikingdev.microsoft_hackathon.repository.chats;

public interface IChatRepository {
    void save(String message, Long userId);
    String findMessage(Long userid);
    void clear(Long userId);
}

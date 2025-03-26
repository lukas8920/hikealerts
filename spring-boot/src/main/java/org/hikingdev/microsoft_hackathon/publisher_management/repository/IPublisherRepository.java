package org.hikingdev.microsoft_hackathon.publisher_management.repository;

import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;

public interface IPublisherRepository {
    Publisher findUserById(Long id);
    Publisher findPublisherByUserId(Long userId);
    void save(Publisher publisher);
}

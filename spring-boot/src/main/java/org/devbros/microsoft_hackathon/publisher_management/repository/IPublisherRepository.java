package org.devbros.microsoft_hackathon.publisher_management.repository;

import org.devbros.microsoft_hackathon.publisher_management.entities.Publisher;

public interface IPublisherRepository {
    Publisher findUserById(Long id);
}

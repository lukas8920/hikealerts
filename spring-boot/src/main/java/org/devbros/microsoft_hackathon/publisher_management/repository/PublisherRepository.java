package org.devbros.microsoft_hackathon.publisher_management.repository;

import org.devbros.microsoft_hackathon.publisher_management.entities.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublisherRepository implements IPublisherRepository {
    private final IPublisherJpaRepository iPublisherJpaRepository;

    @Autowired
    public PublisherRepository(IPublisherJpaRepository iPublisherJpaRepository){
        this.iPublisherJpaRepository = iPublisherJpaRepository;
    }

    @Override
    public Publisher findUserById(Long id) {
        return this.iPublisherJpaRepository.findUserById(id);
    }
}

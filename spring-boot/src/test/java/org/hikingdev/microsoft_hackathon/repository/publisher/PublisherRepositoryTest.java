package org.hikingdev.microsoft_hackathon.repository.publisher;

import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Status;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherJpaRepository;
import org.hikingdev.microsoft_hackathon.repository.users.IUserJpaRepository;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@DataJpaTest
public class PublisherRepositoryTest {
    @Autowired
    private IPublisherJpaRepository publisherJpaRepository;
    @Autowired
    private IUserJpaRepository iUserJpaRepository;

    @Test
    public void testThatFindingPublisherWorks(){
        User user = new User(null, "dummy", null, new ArrayList<>(), false, 2L, null);
        Publisher publisher = new Publisher(2L, "test", Status.COMMUNITY, "copyright", "license");

        this.publisherJpaRepository.save(publisher);
        this.iUserJpaRepository.save(user);

        Publisher outputPublisher = this.publisherJpaRepository.findPublisherByUserId(user.getId());

        assertThat(outputPublisher.getName(), is("test"));
        assertThat(outputPublisher.getCopyright(), is("copyright"));
        assertThat(outputPublisher.getLicense(), is("license"));
    }
}

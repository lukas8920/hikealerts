package org.hikingdev.microsoft_hackathon.repository.users;

import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserJpaRepository extends JpaRepository<User, Long> {
    User findFirstByMail(String mail);
    @Query(value = "SELECT u.mail, p.name, p.status, u.api_key FROM users u INNER JOIN publisher p ON p.id = u.publisher_id WHERE u.id = :id", nativeQuery = true)
    List<Object[]> findProfileById(Long id);
}

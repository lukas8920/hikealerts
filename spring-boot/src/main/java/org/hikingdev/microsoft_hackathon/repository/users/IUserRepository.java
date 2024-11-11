package org.hikingdev.microsoft_hackathon.repository.users;

import org.hikingdev.microsoft_hackathon.user.entities.Profile;
import org.hikingdev.microsoft_hackathon.user.entities.User;

public interface IUserRepository {
    User findById(Long id);
    User findByMail(String mail);
    boolean existsByUsername(String mail);
    User save(User user);
    void deleteById(Long id);
    Profile getProfile(Long id);
}

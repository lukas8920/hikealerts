package org.hikingdev.microsoft_hackathon.repository.users;

import org.hikingdev.microsoft_hackathon.user.entities.Profile;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserRepository implements IUserRepository {
    private final IUserJpaRepository iUserJpaRepository;

    @Autowired
    public UserRepository(IUserJpaRepository iUserJpaRepository){
        this.iUserJpaRepository = iUserJpaRepository;
    }

    @Override
    public User findById(Long id) {
        Optional<User> optional = this.iUserJpaRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public User findByMail(String mail) {
        return this.iUserJpaRepository.findFirstByMail(mail);
    }

    @Override
    public boolean existsByUsername(String mail){
        User user = this.iUserJpaRepository.findFirstByMail(mail);
        return user != null;
    }

    @Override
    public User save(User user) {
        return this.iUserJpaRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        this.iUserJpaRepository.deleteById(id);
    }

    @Override
    public Profile getProfile(Long id) {
        List<Object[]> objects = this.iUserJpaRepository.findProfileById(id);
        if (objects.size() > 0){
            Object[] object = objects.get(0);
            if (object != null && object.length == 4){
                Profile profile = new Profile();
                profile.setMail((String) object[0]);
                profile.setOrganisation((String) object[1]);
                profile.setStatus((String) object[2]);
                if (object[3] == null){
                    profile.setApi_key("-");
                } else {
                    profile.setApi_key("*******************************");
                }
                return profile;
            }
        }

        return null;
    }
}
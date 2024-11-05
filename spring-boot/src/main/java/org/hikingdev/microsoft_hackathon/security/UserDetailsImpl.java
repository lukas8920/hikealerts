package org.hikingdev.microsoft_hackathon.security;

import org.hikingdev.microsoft_hackathon.repository.users.IUserRepository;
import org.hikingdev.microsoft_hackathon.user.entities.Role;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDetailsImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImpl.class.getName());

    private final IUserRepository iUserRepository;

    @Autowired
    public UserDetailsImpl(IUserRepository iUserRepository){
        this.iUserRepository = iUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String profileId) throws UsernameNotFoundException {
        long id;
        try {
            id = Long.parseLong(profileId);
        } catch (NumberFormatException e){
            logger.info("No user found - wrong id format provided.");
            throw new UsernameNotFoundException("No user found - wrong id format provided");
        }

        final User user = this.iUserRepository.findById(id);
        if (user == null){
            logger.info("No user found.");
            throw new UsernameNotFoundException("User with id " + id + " not found");
        }

        List<Role> roles = user.getRoles();
        roles.forEach(role -> logger.info(role.toString()));
        List<SimpleGrantedAuthority> authorities = roles.stream().map(role ->
                new SimpleGrantedAuthority(role.toString())).collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(profileId)
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}

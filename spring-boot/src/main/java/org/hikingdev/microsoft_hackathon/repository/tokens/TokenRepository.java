package org.hikingdev.microsoft_hackathon.repository.tokens;

import org.hikingdev.microsoft_hackathon.user.entities.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenRepository implements ITokenRepository {
    private final ITokenJpaRepository iTokenJpaRepository;

    @Autowired
    public TokenRepository(ITokenJpaRepository iTokenJpaRepository){
        this.iTokenJpaRepository = iTokenJpaRepository;
    }

    @Override
    public void save(Token token) {
        this.iTokenJpaRepository.save(token);
    }

    @Override
    public Token findByToken(String token) {
        return this.iTokenJpaRepository.findFirstByToken(token);
    }
}

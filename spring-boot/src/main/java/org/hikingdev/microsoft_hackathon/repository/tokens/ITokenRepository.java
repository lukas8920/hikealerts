package org.hikingdev.microsoft_hackathon.repository.tokens;

import org.hikingdev.microsoft_hackathon.user.entities.Token;

public interface ITokenRepository {
    void save(Token token);
    Token findByToken(String token);
}

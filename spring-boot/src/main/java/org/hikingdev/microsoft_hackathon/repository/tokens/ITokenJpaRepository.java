package org.hikingdev.microsoft_hackathon.repository.tokens;

import org.hikingdev.microsoft_hackathon.user.entities.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITokenJpaRepository extends CrudRepository<Token, Long> {
    Token findFirstByToken(String token);
}

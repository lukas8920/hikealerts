package org.devbros.microsoft_hackathon.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class KeyTestProvider {
    @Value("event.endpoint.bearer.token")
    private String bearerToken;

    @Bean(name = "bearerToken")
    public String bearerToken(){
        return this.bearerToken;
    }
}

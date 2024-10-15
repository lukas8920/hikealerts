package org.devbros.microsoft_hackathon.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyVaultProvider {
    private static final String PROD_PROFILE = "prod";

    @Value("spring.profiles.active")
    private String activeProfile;

    @Bean(name = "bearerToken")
    public String bearerToken(){
        if (activeProfile.equals(PROD_PROFILE)){
            return "prod";
        }
        return "test";
    }
}

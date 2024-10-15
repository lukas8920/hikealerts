package org.devbros.microsoft_hackathon.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String TEST_PROFILE = "test";

    @Value("spring.profiles.active")
    private String activeProfile;

    @Bean
    public SecurityFilterChain constantTokenSecurityFilterChain(HttpSecurity http, @Qualifier("bearerToken") String bearerToken) throws Exception {
        http
                .securityMatcher("/v1/events/injection")  // Apply this filter chain only to /api/constant-token/** endpoints
                .authorizeHttpRequests(authz -> {
                    if (activeProfile.equals(TEST_PROFILE)){
                        authz.requestMatchers("/v1/event/injection").permitAll();
                    }
                    authz.anyRequest().authenticated();
                })
                .addFilterBefore(new ConstantBearerTokenFilter(bearerToken), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager noopAuthenticationManager() {
        return authentication -> {
            throw new AuthenticationServiceException("Authentication is disabled");
        };
    }
}

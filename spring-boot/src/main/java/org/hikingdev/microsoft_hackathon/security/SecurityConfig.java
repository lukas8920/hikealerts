package org.hikingdev.microsoft_hackathon.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain constantTokenSecurityFilterChain(HttpSecurity http, @Qualifier("bearerToken") String bearerToken) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/v1/events/pull").permitAll();
                    authz.requestMatchers("/v1/map/layer").permitAll();
                    authz.anyRequest().authenticated();
                });

        return http.build();
    }

    @Bean
    public AuthenticationManager noopAuthenticationManager() {
        return authentication -> {
            throw new AuthenticationServiceException("Authentication is disabled");
        };
    }
}

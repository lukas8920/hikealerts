package org.hikingdev.microsoft_hackathon.security;

import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.security.failures.service.LoginAttemptService;
import org.hikingdev.microsoft_hackathon.security.failures.service.RegisterAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Value("${spring.profiles.active}")
    private String profile;


    private final JwtTokenProvider jwtTokenProvider;
    private final Publisher publisher;
    private final LoginAttemptService service;
    private final RegisterAttemptService registerAttemptService;

    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, Publisher publisher,
                          LoginAttemptService service, RegisterAttemptService registerAttemptService){
        this.jwtTokenProvider = jwtTokenProvider;
        this.publisher = publisher;
        this.service = service;
        this.registerAttemptService = registerAttemptService;
    }

    @Bean
    public SecurityFilterChain constantTokenSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtTokenFilter filter = new JwtTokenFilter(registerAttemptService, jwtTokenProvider, publisher, service);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers("/**").permitAll();
                    authz.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    if (!profile.equals("prod")) {
                        authz.requestMatchers("/v3/api-docs").permitAll();
                        authz.requestMatchers("/v3/api-docs.yaml").permitAll();
                    }
                    authz.requestMatchers("/v1/events/pull").permitAll();
                    authz.requestMatchers("/v1/map/layer").permitAll();
                    authz.requestMatchers("/v1/auth/register").permitAll();
                    authz.requestMatchers("/v1/auth/login").permitAll();
                    authz.requestMatchers("/v1/auth/registration_confirm").permitAll();
                    authz.requestMatchers("/v1/user/resetPassword").permitAll();
                    authz.requestMatchers("/v1/user/changePassword").permitAll();
                    authz.anyRequest().authenticated();
                });

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder encoder(){
        return new BCryptPasswordEncoder();
    }
}

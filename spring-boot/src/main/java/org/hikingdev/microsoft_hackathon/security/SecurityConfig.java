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
    private final RoleRegister roleRegister;

    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider, Publisher publisher, RoleRegister roleRegister,
                          LoginAttemptService service, RegisterAttemptService registerAttemptService){
        this.jwtTokenProvider = jwtTokenProvider;
        this.publisher = publisher;
        this.service = service;
        this.registerAttemptService = registerAttemptService;
        this.roleRegister = roleRegister;
    }

    @Bean
    public SecurityFilterChain constantTokenSecurityFilterChain(HttpSecurity http) throws Exception {
        JwtTokenFilter filter = new JwtTokenFilter(registerAttemptService, jwtTokenProvider, publisher, service, roleRegister);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> {
                    authz.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    if (!profile.equals("prod")) {
                        authz.requestMatchers("/v3/api-docs").permitAll();
                        authz.requestMatchers("/v3/api-docs.yaml").permitAll();
                        authz.requestMatchers("/v1/chat/communicate").permitAll();
                        authz.requestMatchers("/v1/geotrek/trail").permitAll();
                        authz.requestMatchers("/v1/geotrek/trails").permitAll();
                    }
                    authz.requestMatchers("/v1/events/pull").permitAll();
                    authz.requestMatchers("/v1/map/layer").permitAll();
                    authz.requestMatchers("/v1/tiles/**").permitAll();
                    authz.requestMatchers("/v1/auth/register").permitAll();
                    authz.requestMatchers("/v1/auth/login").permitAll();
                    authz.requestMatchers("/v1/auth/registration_confirm").permitAll();
                    authz.requestMatchers("/v1/user/resetPassword").permitAll();
                    authz.requestMatchers("/v1/user/changePassword").permitAll();
                    authz.requestMatchers("/v1/chat/init").permitAll();
                    authz.requestMatchers("/v1/geotrek/check").permitAll();
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

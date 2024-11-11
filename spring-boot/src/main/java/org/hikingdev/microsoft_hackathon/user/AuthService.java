package org.hikingdev.microsoft_hackathon.user;

import org.hikingdev.microsoft_hackathon.repository.tokens.ITokenRepository;
import org.hikingdev.microsoft_hackathon.repository.users.IUserRepository;
import org.hikingdev.microsoft_hackathon.security.JwtTokenProvider;
import org.hikingdev.microsoft_hackathon.security.failures.Publisher;
import org.hikingdev.microsoft_hackathon.user.entities.*;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static final Long COMMUNITY_ID = 3L;

    private final IUserRepository iUserRepository;
    private final Publisher publisher;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final ITokenRepository iTokenRepository;

    @Value("${server.host}")
    private String host;
    @Value("${angular.server.port}")
    private String port;

    @Autowired
    public AuthService(Publisher publisher, AuthenticationManager authenticationManager, IUserRepository iUserRepository,
                       JwtTokenProvider jwtTokenProvider, ApplicationEventPublisher applicationEventPublisher, PasswordEncoder passwordEncoder,
                       ITokenRepository iTokenRepository){
        this.publisher = publisher;
        this.authenticationManager = authenticationManager;
        this.iUserRepository = iUserRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.applicationEventPublisher = applicationEventPublisher;
        this.passwordEncoder = passwordEncoder;
        this.iTokenRepository = iTokenRepository;
    }

    public JwtResponse login(LoginRequest loginRequest) throws BadRequestException {
        if (loginRequest.getMail() == null || loginRequest.getPassword() == null
                || loginRequest.getMail().length() > 50 || loginRequest.getPassword().length() > 60){
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("Invalid request parameters");
        }
        User user = this.iUserRepository.findByMail(loginRequest.getMail());
        if (user == null){
            logger.error("User with mail {} is invalid.", loginRequest.getMail());
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("There is no User with the provided mail address.");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getId(), loginRequest.getPassword())
            );
        } catch (AuthenticationException e){
            logger.error("Password for user {} is invalid.", user.getMail());
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("Password is invalid.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtTokenProvider.createUserToken(Long.valueOf(userDetails.getUsername()), Role.USER);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        this.publisher.publishAuthorizationSuccess();
        return new JwtResponse(jwt,
                user.getMail(),
                roles);
    }

    public MessageResponse register(SignupRequest signupRequest) throws BadRequestException {
        if (signupRequest.getMail() == null || signupRequest.getPassword() == null
                || signupRequest.getMail().length() > 50 || signupRequest.getPassword().length() > 60){
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("Invalid request parameters");
        }
        if (this.iUserRepository.existsByUsername(signupRequest.getMail())){
            logger.error("Registration for user {} is invalid as user already exists.", signupRequest.getMail());
            this.publisher.publishRegistrationFailure();
            throw new BadRequestException("Error: User Mail is already assigned to an account.");
        }

        List<Role> roles = List.of(Role.USER);
        User user = new User(null, signupRequest.getMail(), this.passwordEncoder.encode(signupRequest.getPassword())
                , roles, false, COMMUNITY_ID, null);

        user = this.iUserRepository.save(user);
        this.applicationEventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, LocaleContextHolder.getLocale(), "/v1/auth"));
        logger.info("Registration was successful, but still increment to avoid Brute Force on registration endpoint.");
        this.publisher.publishRegistrationFailure();

        return new MessageResponse("Please check your mails to validate your account and to complete the registration.");
    }

    public void createVerificationToken(User user, String token) {
        Token verificationToken = new Token(token, user);
        this.iTokenRepository.save(verificationToken);
    }

    public String confirmRegistration(String token) throws BadRequestException{
        if (token == null || token.length() > 200){
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("Invalid registration token");
        }

        Token verificationToken = this.getVerificationToken(token);
        if (verificationToken == null) {
            logger.error("Verification of account has failed.");
            this.publisher.publishAuthorizationFailure();
            return this.host + ":" + port + "/unauth";
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            logger.error("Verification of account has failed - token has expired.");
            this.publisher.publishAuthorizationFailure();
            return this.host + ":" + port + "/unauth";
        }

        user.setEnabled(true);

        this.iUserRepository.save(user);
        logger.info("Verification of account was successful");
        this.publisher.publishAuthorizationSuccess();

        return this.host + ":" + port + "/login";
    }

    public Token getVerificationToken(String verificationToken) {
        return this.iTokenRepository.findByToken(verificationToken);
    }
}

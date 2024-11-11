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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final ITokenRepository iTokenRepository;
    private final PasswordEncoder encoder;
    private final IUserRepository iUserRepository;
    private final Publisher publisher;
    private final JavaMailSender javaMailSender;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${server.host}")
    private String host;
    @Value("${angular.server.port}")
    private String port;
    @Value("${contact.mail.address}")
    private String contactMail;

    @Autowired
    public UserService(IUserRepository iUserRepository, Publisher publisher, JavaMailSender javaMailSender,
                       ITokenRepository iTokenRepository, PasswordEncoder encoder, JwtTokenProvider jwtTokenProvider){
        this.encoder = encoder;
        this.iTokenRepository = iTokenRepository;
        this.iUserRepository = iUserRepository;
        this.publisher = publisher;
        this.javaMailSender = javaMailSender;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public MessageResponse resetPassword(String userMail) throws BadRequestException {
        if (userMail == null || userMail.length() > 100){
            logger.error("Invalid mail for password reset.");
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("Invalid mail for password reset");
        }

        User user = this.iUserRepository.findByMail(userMail);
        if (user == null){
            logger.error("User with mail " + userMail + " is invalid.");
            this.publisher.publishAuthorizationFailure();
            return new MessageResponse("Please check your mails to reset your password.");
        }
        logger.info("Create reset token for user " + user.getMail());
        this.publisher.publishAuthorizationSuccess();
        String token = UUID.randomUUID().toString();
        createPasswordResetTokenForUser(user, token);
        javaMailSender.send(constructResetTokenEmail(token, user));
        return new MessageResponse("Please check your mails to reset your password.");
    }

    public MessageResponse changePassword(PasswordChange passwordChange) throws BadRequestException {
        if (passwordChange.getNewPassword() == null || passwordChange.getToken() == null
                || passwordChange.getToken().length() > 200 || passwordChange.getNewPassword().length() > 200){
            logger.error("Invalid password change input.");
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("Invalid password change input.");
        }

        final Token passToken = iTokenRepository.findByToken(passwordChange.getToken());

        Calendar cal = Calendar.getInstance();
        if (passToken == null || (passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0){
            logger.error("Password change request for token " + passwordChange.getToken() + " is invalid.");
            this.publisher.publishAuthorizationFailure();
            throw new BadRequestException("No permission to access the hiking alerts service.");
        }

        User user = passToken.getUser();
        String password = encoder.encode(passwordChange.getNewPassword());
        user.setPassword(password);
        this.iUserRepository.save(user);
        logger.info("Changed password for user " + user.getMail());
        this.publisher.publishAuthorizationSuccess();
        javaMailSender.send(constructEmail("Your password has been changed. Please notify us, if you did not trigger the change.", user, "Password changed"));
        return new MessageResponse("ok");
    }

    public MessageResponse savePassword(PasswordChange passwordChange) throws BadRequestException {
        if (passwordChange.getNewPassword() == null || passwordChange.getNewPassword().length() > 100){
            logger.error("Invalid password change input.");
            throw new BadRequestException("Invalid password change input.");
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long user = Long.valueOf(userDetails.getUsername());
        logger.info("User " + user);
        User tmpUser = this.iUserRepository.findById(user);
        if (tmpUser == null) throw new BadRequestException("No permission to change password.");

        String encodedPw = encoder.encode(passwordChange.getNewPassword());
        tmpUser.setPassword(encodedPw);

        this.iUserRepository.save(tmpUser);

        javaMailSender.send(constructEmail("Your password has been changed. Please notify us, if you did not trigger the change.", tmpUser, "Password changed"));
        return new MessageResponse("Password has been successfully changed");
    }

    public MessageResponse refreshApiKey() throws BadRequestException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long user = Long.valueOf(userDetails.getUsername());
        User tmpUser = this.iUserRepository.findById(user);
        if (tmpUser == null) throw new BadRequestException("No permission to change api key.");

        String rawKey= this.jwtTokenProvider.generateApiToken(user);
        String apiKey = encoder.encode(rawKey);
        tmpUser.setApiKey(apiKey);
        this.iUserRepository.save(tmpUser);

        logger.info("Refreshed api key for user " + user);
        return new MessageResponse(rawKey);
    }

    public Profile getProfile() throws BadRequestException{
        logger.info("Request profile information.");
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long user = Long.valueOf(userDetails.getUsername());

        Profile profile = this.iUserRepository.getProfile(user);
        if (profile == null) throw new BadRequestException("No permission to request profile.");

        return profile;
    }

    public MessageResponse deleteUser() throws BadRequestException{
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long user = Long.valueOf(userDetails.getUsername());
        User tmpUser = this.iUserRepository.findById(user);
        if (tmpUser == null) throw new BadRequestException("No permission to delete user.");

        logger.info("Request to delete user " + user);
        this.iUserRepository.deleteById(user);
        logger.info("Deleted user.");
        return new MessageResponse("Successful deletion.");
    }

    private void createPasswordResetTokenForUser(User user, String token) {
        Token myToken = new Token(token, user);
        iTokenRepository.save(myToken);
    }

    private SimpleMailMessage constructResetTokenEmail(String token, User user) {
        String url = this.host + ":" + this.port + "/change-password/" + token;
        String message = "Please click on the link to reset your account.";
        return this.constructEmail(message + " \r\n" + url, user, "Reset Password");
    }

    private SimpleMailMessage constructEmail(String body, User user, String subject) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(user.getMail());
        email.setFrom(contactMail);
        return email;
    }
}

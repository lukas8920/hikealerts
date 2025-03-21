package org.hikingdev.microsoft_hackathon.user;

import io.swagger.v3.oas.annotations.Hidden;
import org.hikingdev.microsoft_hackathon.user.entities.MessageResponse;
import org.hikingdev.microsoft_hackathon.user.entities.PasswordChange;
import org.hikingdev.microsoft_hackathon.user.entities.Profile;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
@RestController
@RequestMapping("/v1/user")
@Hidden
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping(path = "/resetPassword", consumes = "text/plain")
    public ResponseEntity<MessageResponse> resetPw(@RequestBody String mail) throws BadRequestException {
        MessageResponse response = this.userService.resetPassword(mail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<MessageResponse> changePw(@RequestBody PasswordChange passwordChange) throws BadRequestException {
        MessageResponse messageResponse = this.userService.changePassword(passwordChange);
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/refreshApiKey")
    public ResponseEntity<MessageResponse> refreshApiKey() throws BadRequestException {
        MessageResponse messageResponse = this.userService.refreshApiKey();
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<MessageResponse> deleteAccount() throws BadRequestException {
        MessageResponse messageResponse = this.userService.deleteUser();
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/savePassword")
    public ResponseEntity<MessageResponse> savePw(@RequestBody PasswordChange passwordChange) throws BadRequestException {
        MessageResponse messageResponse = this.userService.savePassword(passwordChange);
        return ResponseEntity.ok(messageResponse);
    }

    @GetMapping("/profile")
    public ResponseEntity<Profile> getProfile() throws BadRequestException {
        Profile profile = this.userService.getProfile();
        return ResponseEntity.ok(profile);
    }
}

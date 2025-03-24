package org.hikingdev.microsoft_hackathon.user;

import io.swagger.v3.oas.annotations.Hidden;
import org.hikingdev.microsoft_hackathon.user.entities.JwtResponse;
import org.hikingdev.microsoft_hackathon.user.entities.LoginRequest;
import org.hikingdev.microsoft_hackathon.user.entities.MessageResponse;
import org.hikingdev.microsoft_hackathon.user.entities.SignupRequest;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/v1/auth")
@Hidden
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org", "https://hiking-alerts.org:4200", "https://www.hiking-alerts.org:4200"})
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody SignupRequest signupRequest) throws BadRequestException {
        MessageResponse messageResponse = this.authService.register(signupRequest);
        return ResponseEntity.ok(messageResponse);
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) throws BadRequestException {
        JwtResponse jwtResponse = this.authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
    @GetMapping(value = "/registration_confirm")
    public RedirectView confirmRegistration(@RequestParam("token") String token) throws BadRequestException {
        RedirectView redirectView = new RedirectView();
        String url = this.authService.confirmRegistration(token);
        redirectView.setUrl(url);
        return redirectView;
    }
}

package org.hikingdev.microsoft_hackathon.geotrek;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.hikingdev.microsoft_hackathon.user.UserService;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.exceptions.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/geotrek")
@Hidden
public class GeotrekController {
    private static final String LOGIN_PATH = "/login/?next=/";

    private static final Logger logger = LoggerFactory.getLogger(GeotrekController.class);

    private final GeotrekService geotrekService;
    private final UserService userService;

    @Autowired
    public GeotrekController(GeotrekService geotrekService, UserService userService){
        this.userService = userService;
        this.geotrekService = geotrekService;
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
    @GetMapping("/credentials")
    public ResponseEntity<GeotrekToken> credentials() throws BadRequestException {
        GeotrekToken geotrekToken = this.geotrekService.findToken();
        return ResponseEntity.ok(geotrekToken);
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org", "https://hiking-alerts.org:4200", "https://www.hiking-alerts.org:4200"})
    @GetMapping("/check")
    public ResponseEntity<Void> checkAuthentication(@RequestHeader("X-Original-Method") String method, @RequestHeader("X-Original-URI") String uri, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws InvalidationException {
        logger.info("Check authentication for {} to {}", method, uri);
        if ("POST".equalsIgnoreCase(method) || !uri.equals(LOGIN_PATH)) {
            this.userService.authenticate(authorizationHeader);
        }
        return ResponseEntity.ok().build();
    }
}

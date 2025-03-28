package org.hikingdev.microsoft_hackathon.geotrek;

import io.swagger.v3.oas.annotations.Hidden;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.hikingdev.microsoft_hackathon.user.UserService;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.exceptions.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/geotrek")
@Hidden
public class GeotrekController {
    private static final String LOGIN_PATH = "/login/?next=/";

    private static final Logger logger = LoggerFactory.getLogger(GeotrekController.class);

    private final GeotrekUserService geotrekUserService;
    private final GeotrekTrailService geotrekTrailService;
    private final UserService userService;

    @Autowired
    public GeotrekController(GeotrekUserService geotrekUserService, GeotrekTrailService geotrekTrailService, UserService userService){
        this.userService = userService;
        this.geotrekUserService = geotrekUserService;
        this.geotrekTrailService = geotrekTrailService;
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
    @GetMapping("/credentials")
    public ResponseEntity<GeotrekToken> credentials() throws BadRequestException {
        GeotrekToken geotrekToken = this.geotrekUserService.findToken();
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

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org", "https://hiking-alerts.org:4200", "https://www.hiking-alerts.org:4200"})
    @PostMapping("/trail")
    public ResponseEntity<Void> persistTrail(@RequestBody GeotrekTrail geotrekTrail) throws BadRequestException {
        this.geotrekTrailService.persistTrail(geotrekTrail);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org", "https://hiking-alerts.org:4200", "https://www.hiking-alerts.org:4200"})
    @DeleteMapping("/trail")
    public ResponseEntity<Void> deleteTrail(@RequestParam("id") String id) throws BadRequestException {
        this.geotrekTrailService.deleteTrail(id);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
    @PostMapping("/trail")
    public ResponseEntity<Void> persistTrails(@RequestBody List<GeotrekTrail> geotrekTrails) throws BadRequestException {
        this.geotrekTrailService.persistTrails(geotrekTrails);
        return ResponseEntity.ok().build();
    }
}

package org.hikingdev.microsoft_hackathon.geotrek;

import io.swagger.v3.oas.annotations.Hidden;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekToken;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/geotrek")
@CrossOrigin
@Hidden
public class GeotrekController {
    private final GeotrekService geotrekService;

    @Autowired
    public GeotrekController(GeotrekService geotrekService){
        this.geotrekService = geotrekService;
    }

    @GetMapping("/credentials")
    public ResponseEntity<GeotrekToken> credentials() throws BadRequestException {
        GeotrekToken geotrekToken = this.geotrekService.findToken();
        return ResponseEntity.ok(geotrekToken);
    }

    @GetMapping("/check")
    public ResponseEntity<Void> checkAuthentication(@RequestHeader(value = "X-Original-URI", required = false) String originalUri) {
        return ResponseEntity.ok().build();
    }
}

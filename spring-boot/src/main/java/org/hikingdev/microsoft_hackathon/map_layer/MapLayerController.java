package org.hikingdev.microsoft_hackathon.map_layer;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/map")
@Hidden
public class MapLayerController {
    private final MapLayerService mapLayerService;

    @Autowired
    public MapLayerController(MapLayerService mapLayerService) {
        this.mapLayerService = mapLayerService;
    }

    @CrossOrigin
    @GetMapping("/layer")
    public ResponseEntity<Resource> getJsonLayer() {
        Resource resource = this.mapLayerService.loadJsonLayer();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}

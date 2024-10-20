package org.devbros.microsoft_hackathon.map_layer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MapLayerController {
    private final MapLayerService mapLayerService;

    @Autowired
    public MapLayerController(MapLayerService mapLayerService) {
        this.mapLayerService = mapLayerService;
    }

    @GetMapping
    public ResponseEntity<Resource> getJsonLayer() {
        Resource resource = this.mapLayerService.loadJsonLayer();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}

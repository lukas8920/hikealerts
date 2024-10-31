package org.devbros.microsoft_hackathon.map_layer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/v1/tiles")
public class GeoJsonTileController {
    private static final String geoJsonFilePath = "/app/layer.geojson";

    private final GeoJsonTileService geoJsonTileService;

    @Autowired
    public GeoJsonTileController(GeoJsonTileService geoJsonTileService){
        this.geoJsonTileService = geoJsonTileService;
    }

    @GetMapping("/{z}/{x}/{y}.geojson")
    public ResponseEntity<String> getTile(@PathVariable int z, @PathVariable int x, @PathVariable int y) {
        try {
            String geoJson = new String(Files.readAllBytes(Paths.get(geoJsonFilePath)));

            // Generate the tile by filtering features based on coordinates
            String geoJsonTile = geoJsonTileService.generateTile(geoJson, z, x, y);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/geo+json");
            return new ResponseEntity<>(geoJsonTile, headers, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

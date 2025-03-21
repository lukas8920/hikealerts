package org.hikingdev.microsoft_hackathon.map_layer;

import io.swagger.v3.oas.annotations.Hidden;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RestController()
@RequestMapping("/v1/tiles")
@Hidden
public class TileController {
    private static final Logger logger = LoggerFactory.getLogger(TileController.class);

    private final TileVectorService tileVectorService;

    @Autowired
    public TileController(TileVectorService tileVectorService){
        this.tileVectorService = tileVectorService;
    }

    @CrossOrigin(origins = {"https://hiking-alerts.org", "https://www.hiking-alerts.org"})
    @GetMapping("/{z}/{x}/{y}.pbf")
    public ResponseEntity<InputStreamResource> getTile(@PathVariable("z") int z, @PathVariable("x") int x, @PathVariable("y") int y) throws BadRequestException {
        byte[] tileData = this.tileVectorService.query(z, x, y);
        logger.debug("Query: {}, {}, {}", z, x, y);

        if (tileData == null) {
            return ResponseEntity.notFound().build();
        }
        logger.debug("Retrieved tile data from cache.");
        InputStream tileStream = new ByteArrayInputStream(tileData);
        InputStreamResource inputStreamResource = new InputStreamResource(tileStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/x-protobuf")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(tileData.length))
                .body(inputStreamResource);
    }
}

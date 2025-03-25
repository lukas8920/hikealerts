package org.hikingdev.microsoft_hackathon.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.geodata.Math;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class GeotrekTrailService {
    private static final Logger logger = LoggerFactory.getLogger(GeotrekTrailService.class);

    private final GeonamesService geonamesService;
    private final ITrailRepository iTrailRepository;
    private final TrailMapper trailMapper;
    private final String username;

    @Autowired
    public GeotrekTrailService(@Qualifier("geonamesUsername") String geonamesUsername, @Qualifier("GeonamesService") GeonamesService geonamesService, TrailMapper trailMapper,
                               ITrailRepository iTrailRepository){
        this.geonamesService = geonamesService;
        this.iTrailRepository = iTrailRepository;
        this.trailMapper = trailMapper;
        this.username = geonamesUsername;
    }

    public void persistEditorData(GeotrekTrail geotrekTrail) throws BadRequestException {
        if (geotrekTrail.getName() == null || geotrekTrail.getName().trim().equals("") ){
            logger.error("Empty name cannot be referenced in the database.");
            throw new BadRequestException("Empty name cannot be referenced in the database.");
        }
        logger.info("Processing {}", geotrekTrail);

        Trail trail = this.trailMapper.map(geotrekTrail);

        //convert coordinates to WGS84
        LineString lineString = Math.convertToWGS84(geotrekTrail.getCoordinates());
        //determine median coordinate
        Coordinate midPoint = Math.determineMid(lineString);

        GeonamesResponse response = this.geonamesService.countryCode(midPoint.x, midPoint.y, username);

        if (response == null || response.getCountryCode().trim().length() != 2){
            logger.error("No valid country returned by geoname service.");
            throw new BadRequestException("No valid country returned by geoname service.");
        }
        trail.setCountry(response.getCountryCode());
        trail.setMaintainer(geotrekTrail.getMaintainer());

        this.iTrailRepository.save(trail);
        logger.info("Saved geotrek trail with id {}", geotrekTrail.getId());
    }
}

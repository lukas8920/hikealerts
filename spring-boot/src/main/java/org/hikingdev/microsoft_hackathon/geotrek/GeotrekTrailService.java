package org.hikingdev.microsoft_hackathon.geotrek;

import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeonamesResponse;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Trail;
import org.hikingdev.microsoft_hackathon.geotrek.entities.GeotrekTrail;
import org.hikingdev.microsoft_hackathon.publisher_management.entities.Publisher;
import org.hikingdev.microsoft_hackathon.publisher_management.repository.IPublisherRepository;
import org.hikingdev.microsoft_hackathon.repository.trails.ITrailRepository;
import org.hikingdev.microsoft_hackathon.util.exceptions.BadRequestException;
import org.hikingdev.microsoft_hackathon.util.geodata.Math;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeotrekTrailService {
    private static final String COMMUNITY = "Community";
    private static final Logger logger = LoggerFactory.getLogger(GeotrekTrailService.class);

    private final GeonamesService geonamesService;
    private final ITrailRepository iTrailRepository;
    private final TrailMapper trailMapper;
    private final IPublisherRepository iPublisherRepository;
    private final String username;

    @Autowired
    public GeotrekTrailService(@Qualifier("geonamesUsername") String geonamesUsername, @Qualifier("GeonamesService") GeonamesService geonamesService, TrailMapper trailMapper,
                               ITrailRepository iTrailRepository, IPublisherRepository iPublisherRepository){
        this.geonamesService = geonamesService;
        this.iTrailRepository = iTrailRepository;
        this.iPublisherRepository = iPublisherRepository;
        this.trailMapper = trailMapper;
        this.username = geonamesUsername;
    }

    public void persistEditorData(GeotrekTrail geotrekTrail) throws BadRequestException {
        if (geotrekTrail.getName() == null || geotrekTrail.getName().trim().equals("") ){
            logger.error("Empty name cannot be referenced in the database.");
            throw new BadRequestException("Empty name cannot be referenced in the database.");
        }
        logger.info("Processing {}", geotrekTrail);

        //convert coordinates to WGS84
        LineString lineString = Math.convertToWGS84(geotrekTrail.getCoordinates());
        //determine median coordinate
        Coordinate midPoint = Math.determineMid(lineString);
        geotrekTrail.setCoordinates(lineString);

        Trail trail = this.trailMapper.map(geotrekTrail);

        Call<GeonamesResponse> response = this.geonamesService.countryCode(midPoint.x, midPoint.y, username);

        try {
            GeonamesResponse body = response.execute().body();
            if (body == null || body.getCountryCode().trim().length() != 2){
                logger.error("No valid country returned by geoname service.");
                throw new BadRequestException("No valid country returned by geoname service.");
            }
            trail.setCountry(body.getCountryCode());
            trail.setMaintainer(geotrekTrail.getMaintainer());

            GeotrekTrailService.this.iTrailRepository.save(trail);
            logger.info("Saved geotrek trail with id {}", geotrekTrail.getId());
        } catch (IOException e){
            logger.error("Geonames error {}", e.getMessage());
            throw new BadRequestException("Error while requesting country code from geonames service");
        }
    }

    public void deleteTrail(String id) throws BadRequestException {
        if (id == null){
            logger.error("Error for delete request - min 1 and max 3 ids are expected");
            throw new BadRequestException("No valid ids for delete request provided");
        }
        logger.info("Deleting trail {}", id);

        Long user = this.getActiveSecurityContextHolder();
        Publisher publisher = this.iPublisherRepository.findPublisherByUserId(user);

        List<String> publishers = new ArrayList<>();
        publishers.add(publisher.getName());
        if (!publishers.contains(COMMUNITY)) {
            publishers.add(COMMUNITY);
        }

        this.iTrailRepository.delete(id, publishers);
        logger.info("Successfully deleted trails");
    }

    Long getActiveSecurityContextHolder(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.valueOf(userDetails.getUsername());
    }
}

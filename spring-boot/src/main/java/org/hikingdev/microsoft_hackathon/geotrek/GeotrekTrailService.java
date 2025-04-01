package org.hikingdev.microsoft_hackathon.geotrek;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeonamesService;
import org.hikingdev.microsoft_hackathon.geotrek.api.GeotrekDbService;
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
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeotrekTrailService {
    private static final int MIN_TRAILS = 1;
    private static final int MAX_TRAILS = 200;

    private static final String COMMUNITY = "Community";
    private static final Logger logger = LoggerFactory.getLogger(GeotrekTrailService.class);

    private final GeonamesService geonamesService;
    private final ITrailRepository iTrailRepository;
    private final GeotrekDbService geotrekDbService;
    private final TrailMapper trailMapper;
    private final IPublisherRepository iPublisherRepository;
    private final String username;

    @Autowired
    public GeotrekTrailService(@Qualifier("geonamesUsername") String geonamesUsername, @Qualifier("GeonamesService") GeonamesService geonamesService, TrailMapper trailMapper,
                               ITrailRepository iTrailRepository, IPublisherRepository iPublisherRepository,
                               @Qualifier("GeotrekDbService") GeotrekDbService geotrekDbService){
        this.geonamesService = geonamesService;
        this.iTrailRepository = iTrailRepository;
        this.iPublisherRepository = iPublisherRepository;
        this.geotrekDbService = geotrekDbService;
        this.trailMapper = trailMapper;
        this.username = geonamesUsername;
    }

    public void persistTrail(GeotrekTrail geotrekTrail) throws BadRequestException {
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

    public void persistTrails(List<GeotrekTrail> geotrekTrails) throws BadRequestException {
        if (geotrekTrails == null || geotrekTrails.size() > MAX_TRAILS || geotrekTrails.size() < MIN_TRAILS ){
            logger.error("Provided list of trails needs to be within allowed limits.");
            throw new BadRequestException("Invalid input for multiple geotrek trails.");
        }
        for (GeotrekTrail t: geotrekTrails){
            if (!Math.isValidWGS84(t.getCoordinates())) {
                int indexOfTrail = geotrekTrails.indexOf(t);
                logger.info("Invalid linestring format for trail index {}", indexOfTrail);
                throw new BadRequestException("Linestring format for index " + indexOfTrail + " needs to be WGS84");
            }
        };

        logger.info("Valid input, start with database insertion.");
        for(GeotrekTrail geotrekTrail: geotrekTrails){
            Response<Long> response = null;
            try {
                Trail trail = this.trailMapper.map(geotrekTrail);

                LineString convertedLinestring = Math.convertLinestringToEPSG3857(geotrekTrail.getCoordinates());
                geotrekTrail.setCoordinates(convertedLinestring);

                response = this.geotrekDbService.postTrail(geotrekTrail).execute();
                Long id = response.body();
                if (id != null){
                    trail.setTrailId("geotrek-" + id);

                    if (trail.getTrailname() != null && !trail.getTrailname().trim().equals("")
                            && trail.getTrailId() != null){
                        this.iTrailRepository.save(trail);
                    }
                } else {
                    logger.error("No id received for {}", geotrekTrail.getName());
                }
            } catch (IOException e) {
                logger.error("Error while waiting for response from geotrekDbService for {}, {}", geotrekTrail.getId(), e.getMessage());
                if (response != null){
                    Headers headers = response.headers();
                    ResponseBody body = response.errorBody();

                    logger.error("Headers {}", headers);
                    logger.error("Body {}", body);
                }
                throw new BadRequestException("Processing was not possible for " + geotrekTrail.getId());
            }
        }
        logger.info("Successfully saved all trails in the geotrek and ms database");
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

        int rowsDeleted = this.iTrailRepository.delete(id, publishers);

        // delete ensures that only authorized user update potential leftovers of the trail
        if (rowsDeleted > 0){
            logger.info("Successfully deleted trails");
            Long extractedId = Long.valueOf(id.substring(8));
            logger.info("Query connected trails for {}", id);
            try {
                Response<List<GeotrekTrail>> trailResponse = this.geotrekDbService.findTrails(extractedId).execute();
                List<GeotrekTrail> connectedTrails = trailResponse.body();

                // if connected trails greater 1, then there will be connected trails left in the geotrek database
                if (connectedTrails != null && connectedTrails.size() > 1){
                    GeotrekTrail joinedTrail = this.joinGeotrekTrails(connectedTrails);

                    Trail trail = this.trailMapper.map(joinedTrail);
                    this.iTrailRepository.save(trail);
                } else {
                    logger.info("Only one trail was returned - hence, keep the trail deleted.");
                }
            } catch (IOException ioException){
                logger.error("Service request for trails connected to {} failed: {}", extractedId, ioException.getMessage());
                throw new BadRequestException("Error while requesting connected ids.");
            }
        } else {
            logger.info("Did not find trails to delete.");
        }
    }

    GeotrekTrail joinGeotrekTrails(List<GeotrekTrail> geotrekTrails){
        return GeotrekTrail.joinGeotrekTrails(geotrekTrails);
    }

    Long getActiveSecurityContextHolder(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.valueOf(userDetails.getUsername());
    }
}

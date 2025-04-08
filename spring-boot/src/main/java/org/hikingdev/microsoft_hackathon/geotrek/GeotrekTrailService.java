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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

        logger.info("Request country code for: " + midPoint.x + " / " + midPoint.y);
        Call<GeonamesResponse> response = this.geonamesService.countryCode(midPoint.x, midPoint.y, username);

        try {
            GeonamesResponse body = response.execute().body();
            if (body == null || body.getCountryCode().trim().length() != 2){
                logger.error("No valid country returned by geoname service.");
                throw new BadRequestException("No valid country returned by geoname service.");
            }

            Long extractedId = Long.valueOf(geotrekTrail.getId().substring(8));
            logger.info("Query connected trails for {}", extractedId);
            try {
                Response<List<GeotrekTrail>> trailResponse = this.geotrekDbService.findTrails(extractedId).execute();
                List<GeotrekTrail> connectedTrails = trailResponse.body();

                // if connected trails greater 1, then there will be connected trails left in the geotrek database
                Trail trail = connectedTrails != null && connectedTrails.size() > 1
                        ? persistConnectedTrails(connectedTrails, body.getCountryCode())
                        : this.trailMapper.map(geotrekTrail, body.getCountryCode());
                this.iTrailRepository.save(trail);
            } catch (IOException ioException) {
                logger.error("Service request for trails connected to {} failed: {}", extractedId, ioException.getMessage());
                throw new BadRequestException("Error while requesting connected ids.");
            }
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
            int indexOfTrail = geotrekTrails.indexOf(t);
            if (!Math.isValidWGS84(t.getCoordinates())) {
                logger.info("Invalid linestring format for trail index {}", indexOfTrail);
                throw new BadRequestException("Linestring format for index " + indexOfTrail + " needs to be WGS84");
            }
            if (t.getCountry() == null){
                logger.error("No country provided for trail index {}", indexOfTrail);
                throw new BadRequestException("Trail with index " + indexOfTrail + " does not specify a country.");
            }
        };

        logger.info("Valid input, start with database insertion.");
        for(GeotrekTrail geotrekTrail: geotrekTrails){
            Response<GeotrekTrail> response = null;
            try {
                Trail trail = this.trailMapper.map(geotrekTrail, geotrekTrail.getCountry());

                LineString convertedLinestring = Math.convertLinestringToEPSG3857(geotrekTrail.getCoordinates());
                geotrekTrail.setCoordinates(convertedLinestring);

                response = this.geotrekDbService.postTrail(geotrekTrail).execute();
                GeotrekTrail persistedGeotrekTrail = response.body();
                handleDatabaseTrail(geotrekTrail, trail, persistedGeotrekTrail);
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

    private void handleDatabaseTrail(GeotrekTrail incoming, Trail outputTrail, GeotrekTrail existing) {
        if (existing != null){
            if (existing.getCoordinates().getNumPoints() < incoming.getCoordinates().getNumPoints()){
                try {
                    Long extractedId = existing.getId().startsWith("geotrek-") ? Long.valueOf(existing.getId().substring(8)) : Long.valueOf(existing.getId());
                    Response<List<GeotrekTrail>> connectedTrails = this.geotrekDbService.findTrails(extractedId).execute();

                    List<GeotrekTrail> trailsToDelete = connectedTrails.body();
                    if (trailsToDelete != null){
                        AtomicInteger sum = new AtomicInteger();
                        trailsToDelete.forEach(t -> {
                            int i = this.iTrailRepository.delete("geotrek-" + t.getId(), List.of(incoming.getMaintainer()));
                            sum.addAndGet(i);
                        });
                        logger.info("Deleted existing connected trails in the database: {}", sum);
                    } else {
                        logger.error("Error processing null trails to delete.");
                        return;
                    }

                    List<String> ids = trailsToDelete.stream().map(GeotrekTrail::getId).toList();
                    this.geotrekDbService.deleteTrails(ids).execute();

                    Response<GeotrekTrail> trailResponse = this.geotrekDbService.postTrail(incoming).execute();
                    GeotrekTrail persistedTrail = trailResponse.body();
                    handleDatabaseTrail(incoming, outputTrail, persistedTrail);
                } catch (IOException e){
                    logger.error("Error while querying geotrek database {}", e.getMessage());
                }
            } else {
                String id = existing.getId();
                if (id != null){
                    outputTrail.setTrailId(id.startsWith("geotrek-") ? id : "geotrek-" + id);
                    if (outputTrail.getTrailname() != null && !outputTrail.getTrailname().trim().equals("")
                            && outputTrail.getTrailId() != null){
                        this.iTrailRepository.save(outputTrail);
                        logger.info("Trail persisted in both Geotrek DB and MS DB.");
                    }
                } else {
                    logger.error("No id received for {}", incoming.getName());
                }
            }
        } else {
            logger.error("No valid GeotrekTrail ");
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

        Trail oldTrail = this.iTrailRepository.findTrailByTrailId(id);
        if (oldTrail == null){
            logger.error("No Trail exists for id {}", id);
            throw new BadRequestException("No trail in database exists for id " + id);
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
                if (connectedTrails != null && connectedTrails.size() > 1) {
                    //void first trail
                    connectedTrails.get(0).setCoordinates(Math.voidLineString());
                    Trail trail = persistConnectedTrails(connectedTrails, oldTrail.getCountry());
                    this.iTrailRepository.save(trail);
                } else {
                    logger.info("Only one trail was returned - hence, keep the trail deleted.");
                }
            } catch (IOException ioException) {
                logger.error("Service request for trails connected to {} failed: {}", extractedId, ioException.getMessage());
                throw new BadRequestException("Error while requesting connected ids.");
            }
        } else {
            logger.info("Did not find trails to delete.");
        }
    }

    private Trail persistConnectedTrails(List<GeotrekTrail> connectedTrails, String country){
        GeotrekTrail joinedTrail = this.joinGeotrekTrails(connectedTrails);

        LineString lineString = Math.convertToWGS84(joinedTrail.getCoordinates());
        joinedTrail.setCoordinates(lineString);

        return this.trailMapper.map(joinedTrail, country);
    }

    GeotrekTrail joinGeotrekTrails(List<GeotrekTrail> geotrekTrails){
        return GeotrekTrail.joinGeotrekTrails(geotrekTrails);
    }

    Long getActiveSecurityContextHolder(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return Long.valueOf(userDetails.getUsername());
    }
}

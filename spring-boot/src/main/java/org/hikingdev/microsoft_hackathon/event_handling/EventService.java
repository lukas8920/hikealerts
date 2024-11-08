package org.hikingdev.microsoft_hackathon.event_handling;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.IEventInjection;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.OpenAiService;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.*;
import org.hikingdev.microsoft_hackathon.repository.events.IEventRepository;
import org.hikingdev.microsoft_hackathon.repository.raw_events.RawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.users.IUserRepository;
import org.hikingdev.microsoft_hackathon.user.entities.MessageResponse;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.hikingdev.microsoft_hackathon.util.AiException;
import org.hikingdev.microsoft_hackathon.util.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class.getName());

    private static final List<String> supportedCountries = Arrays.asList("NZ", "US");
    private static final List<String> supportedCreatedBys = Arrays.asList("All", "Community", "Official");

    private static final Pattern COORDINATE_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?");

    private final IEventRepository iEventRepository;
    private final IUserRepository iUserRepository;
    private final IEventInjection iEventInjection;
    private final OpenAiService openAiService;
    private final RawEventRepository rawEventRepository;

    @Autowired
    public EventService(IEventRepository iEventRepository, IUserRepository iUserRepository, OpenAiService openAiService,
                        RawEventRepository rawEventRepository, IEventInjection iEventInjection){
        this.iEventRepository = iEventRepository;
        this.iUserRepository = iUserRepository;
        this.openAiService = openAiService;
        this.rawEventRepository = rawEventRepository;
        this.iEventInjection = iEventInjection;
    }

    public List<MapEvent> pullData(int offset, int limit) throws BadRequestException {
        if (limit > 100){
            throw new BadRequestException("Maximum 100 events can be fetched via this endpoint.");
        }
        return this.iEventRepository.findEvents(offset, limit);
    }

    public List<EventResponse> requestEvents(String boundary, String country, LocalDate fromDate, LocalDate toDate, LocalDate createDate,
                                             boolean nullDates, String createdBy, int offset, int limit) throws BadRequestException {
        validateQuery(boundary, country, fromDate, toDate, createdBy, limit);

        Double[] boundaries = parseBoundary(boundary);
        return this.iEventRepository.queryEvents(boundaries, country, fromDate, toDate, createDate, createdBy, nullDates, limit, offset);
    }

    public MessageResponse deleteEvent(int eventId) throws BadRequestException{
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long user = Long.valueOf(userDetails.getUsername());
        User tmpUser = this.iUserRepository.findById(user);
        if (tmpUser == null) throw new BadRequestException("No permission to delete event.");

        boolean isDeleted = this.iEventRepository.deleteByIdAndPublisher((long) eventId, tmpUser.getPublisherId());

        return isDeleted ? new MessageResponse("Event deletion was not possible.") : new MessageResponse("Event was successfully deleted.");
    }

    public MessageResponse publishEvent(String country, String title, String description, LocalDate fromDate, LocalDate toDate) throws BadRequestException, AiException {
        validatePublish(country, title, description, fromDate, toDate);

        RawEvent rawEvent = new RawEvent(country, title, description, fromDate, toDate);
        try {
            this.rawEventRepository.save(rawEvent);
            String jsonString = rawEvent.parseToJson();
            OpenAiEvent openAiEvent = this.openAiService.sendOpenAiRequest(jsonString);
            logger.info("Received openai response for community request: " + title);
            List<Message> messageResponses = this.iEventInjection.injectEvent(List.of(openAiEvent));

            return new MessageResponse(messageResponses.get(0).getMessage());
        } catch (JsonProcessingException e){
            logger.error("Error while parsing the user input: " + e.getMessage());
            throw new BadRequestException("Invalid input for publishing event.");
        } catch (Exception e){
            logger.error("Fatal error ", e);
            throw new BadRequestException("Internal error while parsing input.");
        }
    }

    private Double[] parseBoundary(String boundary){
        Double[] boundaries = new Double[]{};
        if (boundary != null){
            boundaries = new Double[4];
            String[] coordinateArray = boundary.split(",");
            boundaries[0] = Double.parseDouble(coordinateArray[0]);
            boundaries[1] = Double.parseDouble(coordinateArray[1]);
            boundaries[2] = Double.parseDouble(coordinateArray[2]);
            boundaries[3] = Double.parseDouble(coordinateArray[3]);
        }
        return boundaries;
    }

    private void validatePublish(String country, String title, String description, LocalDate fromDate, LocalDate toDate) throws BadRequestException {
        // validate country
        if (country != null && country.length() != 2){
            throw new BadRequestException("Countries need to be provided in format ISO 3166-1 alpha-2.");
        }
        if (country != null && !supportedCountries.contains(country)){
            throw new BadRequestException("Country " + country + " is currently not supported.");
        }

        // validate description
        if (description == null || description.length() < 2){
            throw new BadRequestException("Description needs to be provided.");
        }
        if (description.length() > 2000){
            throw new BadRequestException("Description length cannot exceed 2000 characters");
        }

        // validate title
        if (title == null || title.length() < 2){
            throw new BadRequestException("Title nees to be provided.");
        }
        if (title.length() > 200){
            throw new BadRequestException("Title length cannot exceed 200 characters.");
        }
    }

    private void validateQuery(String boundary, String country, LocalDate fromDate, LocalDate toDate, String createdBy, int limit) throws BadRequestException {
        // validate limit
        if (limit > 100){
            throw new BadRequestException("Maximum 100 events can be returned per request.");
        }

        // validate country
        if (country != null && country.length() != 2){
            throw new BadRequestException("Countries need to be provided in format ISO 3166-1 alpha-2.");
        }
        if (country != null  && !supportedCountries.contains(country)){
            throw new BadRequestException("Country " + country + " is currently not supported.");
        }

        // validate date
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)){
            throw new BadRequestException("To date needs to be greater than the from date.");
        }

        // validate createdBy
        if (!supportedCreatedBys.contains(createdBy)){
            throw new BadRequestException("Valid values for createdBy are [All, Community, Official]");
        }

        // validate boundaries
        if (boundary != null && isValidBoundary(boundary)){
            throw new BadRequestException("Provided boundaries are not valid.");
        }

        // validate that either boundary or country given
        if (boundary == null && country == null){
            throw new BadRequestException("Either country or boundary needs to be provided.");
        }
    }

    public boolean isValidBoundary(String value) {
        String[] pairs = value.split(",");
        if (pairs.length != 4) { // Four pairs means exactly 8 comma-separated values
            return true;
        }

        // Check if each coordinate pair is valid
        for (int i = 0; i < pairs.length; i += 2) {
            String pair = pairs[i].trim() + "," + pairs[i + 1].trim();
            if (!COORDINATE_PATTERN.matcher(pair).matches()) {
                return true;
            }
        }
        return false;
    }
}

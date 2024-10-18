package org.devbros.microsoft_hackathon.event_injection;

import org.devbros.microsoft_hackathon.BadRequestException;
import org.devbros.microsoft_hackathon.event_injection.countries.BaseCountryInjector;
import org.devbros.microsoft_hackathon.event_injection.countries.USInjector;
import org.devbros.microsoft_hackathon.event_injection.entities.Message;
import org.devbros.microsoft_hackathon.event_injection.entities.OpenAiEvent;
import org.devbros.microsoft_hackathon.repository.events.IEventRepository;
import org.devbros.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.devbros.microsoft_hackathon.repository.regions.IRegionRepository;
import org.devbros.microsoft_hackathon.repository.trails.ITrailRepository;
import org.locationtech.jts.io.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class EventInjection implements IEventInjection {
    private final IRawEventRepository iRawEventRepository;
    private final IEventRepository iEventRepository;
    private final ITrailRepository iTrailRepository;
    private final IRegionRepository iRegionRepository;

    private final Pattern dateTimePattern;
    private final Pattern datePattern;
    private final Pattern fourYYYYPattern;

    @Autowired
    public EventInjection(IRawEventRepository iRawEventRepository, IEventRepository iEventRepository,
                          ITrailRepository iTrailRepository, IRegionRepository iRegionRepository){
        this.iRawEventRepository = iRawEventRepository;
        this.iEventRepository = iEventRepository;
        this.iTrailRepository = iTrailRepository;
        this.iRegionRepository = iRegionRepository;

        // Regex pattern for the date-time formats and four occurrences of YYYY
        String DATE_TIME_PATTERN = "^(\\d{2}/\\d{2}/(?:\\d{4}|YYYY) \\d{2}:\\d{2}:\\d{2})$";
        String DATE_PATTERN = "^(\\d{2}/\\d{2}/(?:\\d{4}|YYYY))$";
        String FOUR_YYYY_PATTERN = "^(YYYY)$";

        // Compile regex patterns
        this.dateTimePattern = Pattern.compile(DATE_TIME_PATTERN);
        this.datePattern = Pattern.compile(DATE_PATTERN);
        this.fourYYYYPattern = Pattern.compile(FOUR_YYYY_PATTERN);
    }

    @Override
    public List<Message> injectEvent(List<OpenAiEvent> openAiEvents) {
        List<Message> errorMessages = new ArrayList<>();
        // check valid input parameters before injecting events
        openAiEvents.forEach(openAiEvent -> {
            if (!checkDateTimePattern(openAiEvent.getFromDate())){
                Message message = new Message(openAiEvent.getEventId(), "Invalid fromDatetime format: " + openAiEvent.getFromDate());
                errorMessages.add(message);
                return;
            }
            if (!checkDateTimePattern(openAiEvent.getToDate())){
                Message message = new Message(openAiEvent.getEventId(), "Invalid toDatetime format: " + openAiEvent.getToDate());
                errorMessages.add(message);
                return;
            }
            if ((openAiEvent.getParkName() == null || openAiEvent.getParkName().length() <= 1) && (openAiEvent.getRegion() == null || openAiEvent.getRegion().length() <= 1)){
                Message message = new Message(openAiEvent.getEventId(), "Either provide a park name or a region - park name: " + openAiEvent.getParkName() + " - region: " + openAiEvent.getRegion());
                errorMessages.add(message);
                return;
            }

            BaseCountryInjector injector = openAiEvent.getCountry() != null ? assignCountryInjector(openAiEvent) : null;
            if (openAiEvent.getCountry() == null || openAiEvent.getCountry().length() != 2 || injector == null){
                Message message = new Message(openAiEvent.getEventId(), "Invalid country: " + openAiEvent.getCountry());
                errorMessages.add(message);
                return;
            }

            boolean flag = false;
            try {
                flag = injector.matchTrails(openAiEvent);
            } catch (ParseException e) {
                // not able to create the event, because mid point calculation failed
            }

            if (!flag){
                Message message = new Message(openAiEvent.getEventId(), "Processing the event was not possible because there is no matching source data.");
                errorMessages.add(message);
            }
        });

        if (!errorMessages.isEmpty()){
            return errorMessages;
        }

        return List.of(new Message("0", "All events processed."));
    }

    protected BaseCountryInjector assignCountryInjector(OpenAiEvent openAiEvent) {
        switch (openAiEvent.getCountry()){
            case "US":
                return new USInjector(iRawEventRepository, iEventRepository, iTrailRepository, iRegionRepository);
            default:
                return null;
        }
    }

    private boolean checkDateTimePattern(String datetime){
        return datetime == null || dateTimePattern.matcher(datetime).matches() || datePattern.matcher(datetime).matches()
                || fourYYYYPattern.matcher(datetime).matches();
    }
}

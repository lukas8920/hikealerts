package org.hikingdev.microsoft_hackathon.chat;

import org.hikingdev.microsoft_hackathon.chat.entities.ChatEvent;
import org.hikingdev.microsoft_hackathon.chat.entities.Chat;
import org.hikingdev.microsoft_hackathon.chat.entities.ChatInit;
import org.hikingdev.microsoft_hackathon.event_handling.RemovalService;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.EventInjection;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.OpenAiService;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.Message;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.OpenAiEvent;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities.RawEvent;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.users.IUserRepository;
import org.hikingdev.microsoft_hackathon.security.UserDetailsImpl;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.hikingdev.microsoft_hackathon.util.exceptions.AiException;
import org.hikingdev.microsoft_hackathon.util.exceptions.EventNotFoundException;
import org.hikingdev.microsoft_hackathon.util.exceptions.InvalidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    private static final List<String> countries = List.of("US", "NZ", "IE");

    @Value("${spring.profiles.active}")
    private String profile;

    private final OpenAiService openAiService;
    private final SignalRService signalRService;
    private final EventInjection eventInjection;
    private final IRawEventRepository iRawEventRepository;
    private final IUserRepository iUserRepository;
    private final RemovalService removalService;
    private final UserDetailsImpl userDetails;

    @Autowired
    public ChatService(OpenAiService openAiService, SignalRService signalRService, EventInjection eventInjection,
                       IRawEventRepository iRawEventRepository, IUserRepository iUserRepository, RemovalService removalService,
                       UserDetailsImpl userDetails){
        this.openAiService = openAiService;
        this.signalRService = signalRService;
        this.eventInjection = eventInjection;
        this.iRawEventRepository = iRawEventRepository;
        this.removalService = removalService;
        this.iUserRepository = iUserRepository;
        this.userDetails = userDetails;
    }

    public ChatInit init(){
        Chat chat = new Chat();
        return new ChatInit(chat.getIntro(), chat.getInstruction(), chat.getLogin());
    }

    public void chat(String text){
        long user;
        User tmpUser;
        if (!profile.equals("test")){
            UserDetails userDetails = this.userDetails.getSecurityContext();
            user = Long.parseLong(userDetails.getUsername());
            tmpUser = this.iUserRepository.findById(user);
        } else {
            user = 9L;
            tmpUser = new User(9L, "", "", new ArrayList<>(), true, 3L, "");
        }
        this.signalRService.getLatestMessage(user, () -> processChat(text, tmpUser, user));
    }

    protected void processChat(String text, User tmpUser, Long user){
        Chat chat = new Chat();

        // check input length
        boolean flag = validateTextInput(text, chat, user);
        if (!flag) return;

        this.signalRService.pushFlaggedMessage(chat.getInputEvaluation(), user);
        ChatEvent chatEvent;
        try {
            chatEvent = this.openAiService.setChatRequest(text);
        } catch (AiException aiException){
            this.signalRService.pushFlaggedMessage(chat.getTrailNameError(), user);
            return;
        }

        logger.info("Handle chat input: " + text + " by user " + user);
        if (chatEvent != null && chatEvent.getType().equals("delete")){
            handleEventDeletion(chatEvent, chat, tmpUser.getPublisherId(), user);
        } else if (chatEvent != null && chatEvent.getType().equals("insert")){
            handleEventInsertion(chatEvent, chat, tmpUser.getPublisherId(), user);
        } else {
            this.signalRService.pushFlaggedMessage(chat.getNotProcessable(), user);
        }
    }

    private void handleEventDeletion(ChatEvent chatEvent, Chat chat, Long publisherId, Long userId){
        // validate chat event
        boolean flag = validateDeletion(chatEvent, chat, userId);
        if (!flag) return;

        try {
            this.removalService.removeEvent(chatEvent.getTrailName(), chatEvent.getCountry(), publisherId);
            this.signalRService.pushFlaggedMessage(chat.getDeleteSuccess(), userId);
        } catch (EventNotFoundException e){
            logger.error("Event deletion not possible ", e);
            this.signalRService.pushFlaggedMessage(chat.getNoAlertError(), userId);
        } catch (InvalidationException e){
            logger.error("User is not authorized to delete event ", e);
            this.signalRService.pushFlaggedMessage(chat.getOwnerError(), userId);
        }
    }

    private void handleEventInsertion(ChatEvent chatEvent, Chat chat, Long publisherId, Long userId) {
        boolean flag;
        // validate chat event
        flag = validateInsert(chatEvent, chat, userId);
        if (!flag) return;

        this.signalRService.pushRepeatingMessage(chat.getSearchingGeodata(), chat.getSearchingPatience(), userId);
        // create standard objects
        RawEvent rawEvent = new RawEvent(chatEvent, publisherId);
        OpenAiEvent openAiEvent = new OpenAiEvent(chatEvent, rawEvent.getEventId());

        this.iRawEventRepository.save(rawEvent);
        List<Message> messageResponses = this.eventInjection.injectEvent(List.of(openAiEvent));

        if (messageResponses.get(0).getId().equals("0")){
            this.signalRService.pushFlaggedMessage(chat.getAlertSuccess(), userId);
        } else {
            this.signalRService.pushFlaggedMessage(chat.getSearchError(), userId);
        }
    }

    private boolean validateInsert(ChatEvent chatEvent, Chat chat, Long userId){
        if (chatEvent.getCountry() == null || chatEvent.getCountry().trim().isEmpty()){
            this.signalRService.pushFlaggedMessage(chat.getNoCountryError(), userId);
            return false;
        }
        if (chatEvent.getTrailName() == null || chatEvent.getTrailName().trim().isEmpty()){
            this.signalRService.pushFlaggedMessage(chat.getTrailNameError(), userId);
            return false;
        }
        if (!countries.contains(chatEvent.getCountry())){
            this.signalRService.pushFlaggedMessage(chat.getCountryError(), userId);
            return false;
        }
        return true;
    }

    private boolean validateDeletion(ChatEvent chatEvent, Chat chat, Long userId){
        if (chatEvent.getTrailName() == null || chatEvent.getTrailName().trim().isEmpty()){
            this.signalRService.pushFlaggedMessage(chat.getNoAlertError(), userId);
            return false;
        }
        if (chatEvent.getCountry() == null || chatEvent.getCountry().trim().isEmpty()){
            this.signalRService.pushFlaggedMessage(chat.getNoCountryError(), userId);
            return false;
        }
        return true;
    }

    private boolean validateTextInput(String text, Chat chat, Long userId) {
        if (text == null) {
            logger.info("message is null.");
            this.signalRService.pushFlaggedMessage(chat.getMinTextError(), userId);
            return false;
        }
        int lengthOfText = text.length();
        int noOfWords = text.trim().split("\\s+").length;
        if (noOfWords <= 3){
            logger.info("Message does not contain enough words.");
            this.signalRService.pushFlaggedMessage(chat.getMinTextError(), userId);
            return false;
        }
        if (noOfWords > 200 || lengthOfText > 500){
            this.signalRService.pushFlaggedMessage(chat.getMaxTextError(), userId);
            return false;
        }
        return true;
    }
}

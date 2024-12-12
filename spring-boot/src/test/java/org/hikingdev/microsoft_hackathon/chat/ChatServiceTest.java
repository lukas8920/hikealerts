package org.hikingdev.microsoft_hackathon.chat;

import org.hikingdev.microsoft_hackathon.chat.entities.Chat;
import org.hikingdev.microsoft_hackathon.chat.entities.ChatEvent;
import org.hikingdev.microsoft_hackathon.event_handling.RemovalService;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.EventInjection;
import org.hikingdev.microsoft_hackathon.event_handling.event_injection.OpenAiService;
import org.hikingdev.microsoft_hackathon.repository.raw_events.IRawEventRepository;
import org.hikingdev.microsoft_hackathon.repository.users.IUserRepository;
import org.hikingdev.microsoft_hackathon.security.UserDetailsImpl;
import org.hikingdev.microsoft_hackathon.user.entities.User;
import org.hikingdev.microsoft_hackathon.util.AiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatServiceTest {
    private OpenAiService openAiService;
    private ChatService chatService;

    private Consumer<String> pushMessage;
    private Chat chat;
    private TestSignalRService testFirebaseService;
    private User user;

    @BeforeEach
    private void setup(){
        EventInjection eventInjection = mock(EventInjection.class);
        IRawEventRepository iRawEventRepository = mock(IRawEventRepository.class);
        IUserRepository iUserRepository = mock(IUserRepository.class);
        RemovalService removalService = mock(RemovalService.class);
        UserDetailsImpl userDetailsImpl = mock(UserDetailsImpl.class);

        openAiService = mock(OpenAiService.class);
        chat = new Chat();
        testFirebaseService = new TestSignalRService();

        user = new User();
        user.setPublisherId(1L);

        when(iUserRepository.findById(1L)).thenReturn(user);

        chatService = new ChatService(openAiService, testFirebaseService, eventInjection, iRawEventRepository, iUserRepository, removalService, userDetailsImpl);
    }

    @Test
    public void testInvalidInputLength(){
        pushMessage = s -> assertThat(s, is(chat.getMinTextError()));
        this.chatService.processChat(null, user, 1L);

        pushMessage = s -> assertThat(s, is(chat.getMinTextError()));
        this.chatService.processChat("echo", user, 1L);

        int wordCount = 201;
        pushMessage = s -> assertThat(s, is(chat.getMaxTextError()));
        this.chatService.processChat(" test".repeat(wordCount), user, 1L);

        int charCount = 501;
        pushMessage = s -> assertThat(s, is(chat.getMaxTextError()));
        String text = "t".repeat(charCount) + " t t t t t";
        this.chatService.processChat(text, user, 1L);
    }

    @Test
    public void testInsertionInvalidCountry() throws AiException {
        testFirebaseService.triggerCounter = 1;
        pushMessage = s-> assertThat(s, is(chat.getNoCountryError()));

        ChatEvent chatEvent = new ChatEvent();
        chatEvent.setCountry("");
        chatEvent.setType("insert");

        when(openAiService.setChatRequest("test test test test test test")).thenReturn(chatEvent);
        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));

        testFirebaseService.counter = 0;
        testFirebaseService.executed = false;

        chatEvent.setCountry(null);

        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));

        testFirebaseService.counter = 0;
        testFirebaseService.executed = false;

        chatEvent.setCountry("DD");
        chatEvent.setTrailName("dummy");

        pushMessage = s-> assertThat(s, is(chat.getCountryError()));

        when(openAiService.setChatRequest("test test test test test test")).thenReturn(chatEvent);
        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));
    }

    @Test
    public void testInsertionInvalidTrail() throws AiException {
        testFirebaseService.triggerCounter = 1;
        pushMessage = s-> assertThat(s, is(chat.getTrailNameError()));

        ChatEvent chatEvent = new ChatEvent();
        chatEvent.setCountry("NZ");
        chatEvent.setTrailName("");
        chatEvent.setType("insert");

        when(openAiService.setChatRequest("test test test test test test")).thenReturn(chatEvent);
        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));

        testFirebaseService.counter = 0;
        testFirebaseService.executed = false;

        chatEvent.setTrailName(null);

        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));
    }

    @Test
    public void testDeletionInvalidTrail() throws AiException {
        testFirebaseService.triggerCounter = 1;
        pushMessage = s-> assertThat(s, is(chat.getNoAlertError()));

        ChatEvent chatEvent = new ChatEvent();
        chatEvent.setCountry("NZ");
        chatEvent.setTrailName("");
        chatEvent.setType("delete");

        when(openAiService.setChatRequest("test test test test test test")).thenReturn(chatEvent);
        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));

        testFirebaseService.counter = 0;
        testFirebaseService.executed = false;

        chatEvent.setTrailName(null);

        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));
    }

    @Test
    public void testDeletionInvalidCountry() throws AiException {
        testFirebaseService.triggerCounter = 1;
        pushMessage = s-> assertThat(s, is(chat.getNoCountryError()));

        ChatEvent chatEvent = new ChatEvent();
        chatEvent.setCountry("");
        chatEvent.setTrailName("dummy");
        chatEvent.setType("delete");

        when(openAiService.setChatRequest("test test test test test test")).thenReturn(chatEvent);
        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));

        testFirebaseService.counter = 0;
        testFirebaseService.executed = false;

        chatEvent.setCountry(null);

        this.chatService.processChat("test test test test test test", user, 1L);
        assertThat(testFirebaseService.executed, is(true));
    }

    @Test
    public void test() throws IOException {

    }

    class TestSignalRService extends SignalRService {
        int triggerCounter = 0;
        int counter = 0;
        boolean executed = false;

        public TestSignalRService() {
            super(null, null, null, null);
        }

        @Override
        public void pushFlaggedMessage(String message, Long userId){
            if (triggerCounter == counter){
                ChatServiceTest.this.pushMessage.accept(message);
                executed = true;
            } else {
                counter += 1;
            }
        }
    }
}

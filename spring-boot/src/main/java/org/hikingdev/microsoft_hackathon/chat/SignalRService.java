package org.hikingdev.microsoft_hackathon.chat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.hikingdev.microsoft_hackathon.chat.entities.SignalRConnectionInfo;
import org.hikingdev.microsoft_hackathon.chat.entities.SignalRMessage;
import org.hikingdev.microsoft_hackathon.repository.chats.ChatRepository;
import org.hikingdev.microsoft_hackathon.security.JwtTokenProvider;
import org.hikingdev.microsoft_hackathon.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SignalRService {
    private static final Logger logger = LoggerFactory.getLogger(SignalRService.class);

    private final AtomicBoolean flag = new AtomicBoolean(false);
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatRepository chatRepository;
    private final UserDetailsImpl userDetails;
    private final String signalRServiceBaseEndpoint;

    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${signalr.hubname}")
    private String hubName;

    @Autowired
    public SignalRService(JwtTokenProvider jwtTokenProvider, ChatRepository chatRepository, UserDetailsImpl userDetails,
                          @Qualifier("signalrEndpoint") String signalrEndpoint){
        this.jwtTokenProvider = jwtTokenProvider;
        this.chatRepository = chatRepository;
        this.userDetails = userDetails;
        this.signalRServiceBaseEndpoint = signalrEndpoint;
    }

    public SignalRConnectionInfo getConnectionInfo(){
        long user;
        if (!profile.equals("test")){
            UserDetails userDetails = this.userDetails.getSecurityContext();
            user = Long.parseLong(userDetails.getUsername());
        } else {
            user = 9L;
        }
        String hubUrl = signalRServiceBaseEndpoint + "/client/?hub=" + hubName + "_" + user;
        String accessKey = this.jwtTokenProvider.generateSignalRToken(hubUrl, String.valueOf(user));
        return new SignalRConnectionInfo(hubUrl, accessKey);
    }

    public void pushFlaggedMessage(String text, Long userId){
        flag.set(true);
        this.pushMessage(text, userId);
    }

    private void pushMessage(String text, Long userId){
        // Create a new message object with the required data
        //todo replace jwt token with connection string
        String hubUrl = signalRServiceBaseEndpoint + "/api/v1/hubs/" + hubName + "_" + userId;
        String accessKey = jwtTokenProvider.generateSignalRToken(hubUrl, String.valueOf(userId));

        this.chatRepository.save(text, userId);
        // Prepare the message body
        SignalRMessage message = new SignalRMessage("newMessage", new Object[] { text });

        HttpResponse<?> response = Unirest.post(hubUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessKey)
                .body(message)
                .asEmpty();

        if (response.getStatus() == 200 || response.getStatus() == 202) {
            logger.info("Push request was successful!");
        } else {
            logger.error("SignalR push request failed with status code: " + response.getStatus());
        }
    }

    public void pushRepeatingMessage(String text1, String text2, Long userId){
        AtomicInteger counter = new AtomicInteger();
        flag.set(false);
        pushMessage(text1, userId);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e){
                return;
            }
            while (!flag.get() && counter.get() <= 7){
                pushMessage(text2, userId);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e){
                    break;
                }
                counter.addAndGet(1);
            }
        }).start();
    }

    public void getLatestMessage(Long userId, Runnable runnable){
        String lastMessage = this.chatRepository.findMessage(userId);

        if (lastMessage == null){
            runnable.run();
            // clear cache after successful run
            this.chatRepository.clear(userId);
        } else {
            // re-post last message
            this.pushFlaggedMessage(lastMessage, userId);
        }
    }
}

package org.hikingdev.microsoft_hackathon.chat;

import org.hikingdev.microsoft_hackathon.chat.entities.ChatInit;
import org.hikingdev.microsoft_hackathon.chat.entities.SignalRConnectionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final SignalRService signalRService;

    @Autowired
    public ChatController(ChatService chatService, SignalRService signalRService){
        this.chatService = chatService;
        this.signalRService = signalRService;
    }

    @CrossOrigin
    @PostMapping("/communicate")
    public ResponseEntity<String> chat(@RequestBody String message){
        this.chatService.chat(message);
        return ResponseEntity.ok().body("Processed Message");
    }

    // for test purposes, add "http://localhost:8081", "http://localhost:4200"
    @CrossOrigin(origins = {"https://hiking-alerts.org"}, allowCredentials = "true")
    @PostMapping("/negotiate")
    public ResponseEntity<SignalRConnectionInfo> negotiate(){
        SignalRConnectionInfo signalRConnectionInfo = this.signalRService.getConnectionInfo();
        return ResponseEntity.ok(signalRConnectionInfo);
    }

    @CrossOrigin
    @GetMapping("/init")
    public ResponseEntity<ChatInit> init(){
        ChatInit chatInit = this.chatService.init();
        return ResponseEntity.ok(chatInit);
    }
}

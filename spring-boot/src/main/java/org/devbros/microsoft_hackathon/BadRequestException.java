package org.devbros.microsoft_hackathon;

import lombok.Getter;
import lombok.Setter;
import org.devbros.microsoft_hackathon.event_handling.event_injection.entities.Message;

import java.util.List;

@Getter
@Setter
public class BadRequestException extends Exception {
    private List<Message> messages;
    public BadRequestException(String message){super(message);}

    public BadRequestException(List<Message> messages){
        this.messages = messages;
    }
}

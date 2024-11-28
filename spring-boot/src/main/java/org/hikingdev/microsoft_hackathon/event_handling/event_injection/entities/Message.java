package org.hikingdev.microsoft_hackathon.event_handling.event_injection.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String country;
    private String message;

    public Message(String id, String message){
        this.id = id;
        this.message = message;
    }
}

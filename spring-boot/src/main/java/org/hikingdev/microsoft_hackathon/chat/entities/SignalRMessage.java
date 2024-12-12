package org.hikingdev.microsoft_hackathon.chat.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignalRMessage {
    private String target;
    private Object[] arguments;
}

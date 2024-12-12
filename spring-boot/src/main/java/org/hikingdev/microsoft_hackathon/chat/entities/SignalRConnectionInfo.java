package org.hikingdev.microsoft_hackathon.chat.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SignalRConnectionInfo {
    private String url;
    private String accessToken;
}

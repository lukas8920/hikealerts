package org.hikingdev.microsoft_hackathon.user.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    private String mail;
    private String password;
}

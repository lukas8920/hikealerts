package org.hikingdev.microsoft_hackathon.user.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChange {
    private String newPassword;
    private String token;
}

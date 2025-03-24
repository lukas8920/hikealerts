package org.hikingdev.microsoft_hackathon.user.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private List<String> roles;
    private long expiryTimestamp;

    public JwtResponse(String jwt, String email, List<String> roles, long expiryTimestamp){
        this.token = jwt;
        this.email = email;
        this.roles = roles;
        this.expiryTimestamp = expiryTimestamp;
    }
}

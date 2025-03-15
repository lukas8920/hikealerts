package org.hikingdev.microsoft_hackathon.geotrek.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GeotrekUser {
    private Long id;
    private String password;
    private LocalDateTime lastLogin;
    private boolean isSuperuser;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isStaff;
    private boolean isActive;
    private LocalDateTime dateJoined;
}

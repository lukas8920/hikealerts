package org.hikingdev.microsoft_hackathon.user.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Profile {
    private String mail;
    private String organisation;
    private String status;
    private String api_key;

    public void setStatus(String status){
        if (status.equals("COMMUNITY")){
            this.status = "Community";
        } else if (status.equals("OFFICIAL")){
            this.status = "Official";
        } else {
            this.status = status;
        }
    }
}

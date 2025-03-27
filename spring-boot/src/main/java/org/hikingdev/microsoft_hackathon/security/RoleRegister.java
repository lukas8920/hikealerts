package org.hikingdev.microsoft_hackathon.security;

import lombok.Getter;
import lombok.Setter;
import org.hikingdev.microsoft_hackathon.user.entities.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
public class RoleRegister {
    private List<String> UI_USER_ENDPOINTS = List.of(
            "/v1/user",
            "/v1/chat/negotiate",
            "/v1/chat/communicate",
            "/v1/geotrek/check",
            "/v1/geotrek/credentials"
    );

    private List<String> API_USER_ENDPOINTS = List.of(
            "/v1/events/query",
            "/v1/events/delete"
    );

    private List<String> API_PUBLISHER_ENDPOINTS = List.of(
            "/v1/events/publish"
    );

    private List<String> GEOTREK_ENDPOINTS = List.of(
            "/v1/geotrek/trail"
    );

    private Map<Role, List<String>> roles = Map.of(
            Role.UI_USER, UI_USER_ENDPOINTS,
            Role.API_USER, API_USER_ENDPOINTS,
            Role.GEOTREK, GEOTREK_ENDPOINTS,
            Role.API_PUBLISHER, API_PUBLISHER_ENDPOINTS
    );
}

package org.hikingdev.microsoft_hackathon;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = @Server(url = "https://www.hiking-alerts.org:8080", description = "Prod server"),
        security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        paramName = "Authorization",
        bearerFormat = "JWT",
        description = "Create an account to access your personal bearer token. In the overview tab you can manage your token."
)
public class OpenapiConfig {
}

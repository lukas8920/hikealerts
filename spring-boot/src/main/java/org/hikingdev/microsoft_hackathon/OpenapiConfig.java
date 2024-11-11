package org.hikingdev.microsoft_hackathon;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        servers = @Server(url = "https://www.hiking-alerts.org:8080"),
        security = { @SecurityRequirement(name = "Authorization") },
        info = @Info(
                title = "Hiking Alerts API",
                description = "Hiking Alerts API",
                version = "1.0.0",
                termsOfService = "https://hiking-alerts.org/terms-of-service",
                license = @License(name = "MIT", url = "https://spdx.org/licenses/MIT"),
                contact = @Contact(name = "Support", url = "www.hiking-alerts.org", email = "info.hikingalerts@gmail.com")
        )
)
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        paramName = "Authorization",
        bearerFormat = "JWT",
        description = "Create an account to access your personal bearer token. In the overview tab you can manage your token."
)
public class OpenapiConfig {
}

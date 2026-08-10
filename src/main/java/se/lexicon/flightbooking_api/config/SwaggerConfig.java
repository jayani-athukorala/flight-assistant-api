package se.lexicon.flightbooking_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Flight Booking API")
                        .version("1.0.0")
                        .description("""
                                REST API for managing flights, seats and bookings.

                                Authentication:
                                - Register a user using /api/auth/register
                                - Login using /api/auth/login
                                - Copy the JWT returned from login
                                - Click the Authorize button in Swagger UI
                                - Enter: Bearer <your-jwt-token>

                                Authenticated endpoints require a valid JWT.
                                """)
                        .contact(new Contact()
                                .name("Flight Booking API Team"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))

                // Adds the JWT security requirement globally
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )

                // Defines the Bearer JWT authentication scheme
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name(SECURITY_SCHEME_NAME)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Enter your JWT token. " +
                                                                "Example: Bearer eyJhbGciOiJIUzI1NiIs..."
                                                )
                                )
                );
    }
}


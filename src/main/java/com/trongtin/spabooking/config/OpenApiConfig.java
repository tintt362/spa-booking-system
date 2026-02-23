package com.trongtin.spabooking.config;



import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${info.app.name:Spa Booking System}")
    private String appName;

    @Value("${info.app.description:Complete booking management system}")
    private String appDescription;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(apiServers())
                .tags(apiTags())
                .components(securityComponents())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info apiInfo() {
        return new Info()
                .title(appName)
                .description(appDescription)
                .version(appVersion)
                .contact(new Contact()
                        .name("Development Team")
                        .email("dev@spa-booking.com")
                        .url("https://spa-booking.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> apiServers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8080")
                        .description("Development Server"),
                new Server()
                        .url("https://staging-api.spa-booking.com")
                        .description("Staging Server"),
                new Server()
                        .url("https://api.spa-booking.com")
                        .description("Production Server")
        );
    }

    private List<Tag> apiTags() {
        return Arrays.asList(
                new Tag()
                        .name("Authentication")
                        .description("User authentication and registration endpoints"),
                new Tag()
                        .name("Public Services")
                        .description("Publicly accessible service information"),
                new Tag()
                        .name("Bookings - Anonymous")
                        .description("Booking endpoints for non-registered users"),
                new Tag()
                        .name("Bookings - User")
                        .description("Booking management for authenticated users"),
                new Tag()
                        .name("User Profile")
                        .description("User profile and account management"),
                new Tag()
                        .name("Loyalty Program")
                        .description("Loyalty points and membership management"),
                new Tag()
                        .name("Admin - Dashboard")
                        .description("Admin dashboard and statistics"),
                new Tag()
                        .name("Admin - Bookings")
                        .description("Admin booking management"),
                new Tag()
                        .name("Admin - Users")
                        .description("Admin user management"),
                new Tag()
                        .name("Admin - Services")
                        .description("Admin service management"),
                new Tag()
                        .name("Admin - Therapists")
                        .description("Admin therapist management"),
                new Tag()
                        .name("Admin - Reports")
                        .description("Admin reports and analytics")
        );
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .name("Bearer Authentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/auth/login endpoint")
                );
    }
}
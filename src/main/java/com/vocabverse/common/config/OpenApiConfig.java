package com.vocabverse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI vocabVerseOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("/api/v1")
                        .description("VocabVerse API v1")))
                .info(new Info()
                        .title("VocabVerse API")
                        .description("VocabVerse Backend REST API")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .pathsToMatch("/users/**")
                .build();
    }

    @Bean
    public GroupedOpenApi collectionApi() {
        return GroupedOpenApi.builder()
                .group("collection")
                .pathsToMatch("/collections/**", "/public/collections/**")
                .build();
    }

    @Bean
    public GroupedOpenApi vocabularyApi() {
        return GroupedOpenApi.builder()
                .group("vocabulary")
                .pathsToMatch("/vocabularies/**", "/collections/*/vocabularies/**")
                .build();
    }

    @Bean
    public GroupedOpenApi learningApi() {
        return GroupedOpenApi.builder()
                .group("learning")
                .pathsToMatch(
                        "/learning/**",
                        "/reviews/**",
                        "/flashcards/**",
                        "/quizzes/**",
                        "/typing/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi notificationApi() {
        return GroupedOpenApi.builder()
                .group("notification")
                .pathsToMatch("/notifications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("ai")
                .pathsToMatch("/ai/**", "/roleplay/**")
                .build();
    }

    @Bean
    public GroupedOpenApi exportApi() {
        return GroupedOpenApi.builder()
                .group("export")
                .pathsToMatch("/export/**")
                .build();
    }
}

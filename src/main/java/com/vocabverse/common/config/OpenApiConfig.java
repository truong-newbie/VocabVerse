package com.vocabverse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vocabVerseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VocabVerse API")
                        .description("VocabVerse Backend REST API")
                        .version("v1"));
    }
}

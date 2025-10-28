package com.example.chatservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI api() {
    final String schemeName = "apiKeyAuth";
    return new OpenAPI()
        .info(new Info().title("Chat Service API").version("v1"))
        .addSecurityItem(new SecurityRequirement().addList(schemeName))
        .components(new Components()
            .addSecuritySchemes(schemeName, new SecurityScheme()
                .name("X-API-KEY")
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)));
  }
}

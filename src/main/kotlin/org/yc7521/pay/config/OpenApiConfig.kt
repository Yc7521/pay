package org.yc7521.pay.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.In
import io.swagger.v3.oas.models.security.SecurityRequirement

@Configuration
class OpenApiConfig {
  @Bean
  fun springOpenAPI() : OpenAPI {
    return OpenAPI()
      .components(Components()
        .addSecuritySchemes(
          "bearer-jwt",
          SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .`in`(In.HEADER)
            .name("Authorization")))
      .info(Info().title("Pay App API").version("0.0.1"))
      .addSecurityItem(
        SecurityRequirement().addList("bearer-jwt", listOf("read", "write")));
  }
}
package org.yc7521.pay.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.*
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.security.SecurityScheme.In
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class OpenApiConfig {
  @Bean
  fun springOpenAPI(): OpenAPI {
    return OpenAPI()
      .components(
        Components()
          .addSecuritySchemes(
            "bearer-jwt",
            SecurityScheme()
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")
              .`in`(In.HEADER)
              .name("Authorization")
          )
      )
      .info(Info().title("Pay App API").version("0.0.1"))
      .addSecurityItem(
        SecurityRequirement().addList("bearer-jwt", listOf("read", "write"))
      )
  }

  @Bean
  fun consumerTypeHeaderOpenAPICustomiser(): OpenApiCustomiser? {
    return OpenApiCustomiser { openApi: OpenAPI ->
      openApi.paths.values
        .stream()
        .flatMap {
          it.readOperations().stream()
        }
        .forEach {
          it.responses
            .addApiResponse(
              "400",
              ApiResponse()
                .description("Bad Request")
                .content(
                  Content()
                    .addMediaType(
                      "application/json",
                      MediaType().schema(
                        MapSchema().properties(
                          mapOf(
                            "msg" to StringSchema(),
                            "type" to StringSchema(),
                          )
                        )
                      )
                    )
                )
            )
            .addApiResponse(
              "500",
              ApiResponse()
                .description("Internal Server Error")
                .content(
                  Content()
                    .addMediaType(
                      "application/json",
                      MediaType().schema(
                        MapSchema().properties(
                          mapOf(
                            "msg" to StringSchema(),
                            "stack" to ArraySchema().items(StringSchema()),
                            "type" to StringSchema(),
                          )
                        )
                      )
                    )
                )
            )
        }
    }
  }
}
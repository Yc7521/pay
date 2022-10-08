package org.yc7521.pay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

import static org.yc7521.pay.config.JwtConstants.HEADER_STRING;

@Configuration
@EnableOpenApi
public class SwaggerConfig {
  @Bean
  public Docket createRestApi() {
    return new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo("Security Server", "3.0"))
      .useDefaultResponseMessages(true)
      .forCodeGeneration(false)
      .select()
      //扫描的路径包,设置basePackage会将包下的所有被@Api标记类的所有方法作为api
      .apis(RequestHandlerSelectors.basePackage("org.yc7521.pay.api"))
      //指定路径处理PathSelectors.any()代表所有的路径
      .paths(PathSelectors.any())
      .build()
      /* 设置安全模式，swagger可以设置访问token */
      .securitySchemes(securitySchemes())
      .securityContexts(securityContexts());
  }

  private ApiInfo apiInfo(String title, String version) {
    return new ApiInfoBuilder()
      .title(title)
      .description("des")
      .termsOfServiceUrl("#")
      .version(version)
      .build();
  }

  /**
   * 安全模式，这里指定token通过Authorization头请求头传递
   */
  private List<SecurityScheme> securitySchemes() {
    List<SecurityScheme> apiKeyList = new ArrayList<>();
    apiKeyList.add(new ApiKey("Authorization", "Authorization", "header"));
    return apiKeyList;
  }

  private List<SecurityContext> securityContexts() {
    List<SecurityContext> securityContexts = new ArrayList<>();
    securityContexts.add(SecurityContext
      .builder()
      .securityReferences(defaultAuth())
      .forPaths(PathSelectors.regex("^(?!auth).*$"))
      .build());
    return securityContexts;
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope =
      new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    List<SecurityReference> securityReferences = new ArrayList<>();
    securityReferences.add(new SecurityReference(HEADER_STRING, authorizationScopes));
    return securityReferences;
  }
}
package com.ddcj.binteum.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info apiInfo = new Info()
                .version("v1.0.0")
                .title("Binteum API")
                .description("Binteum API Documentation");

        String jwtSchemeName = "BearerToken";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Binteum Local Server");

        // 백엔드 서버 배포 후 수정 및 아래 리스트에 추가
        /*Server httpServer = new Server()
                .url("https://spring.issueissyu.cloud")
                .description("IssueIssyu Prod Server");*/


        return new OpenAPI()
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(components)
                .servers(List.of(localServer));
                //.servers(List.of(localServer, httpServer));
    }

    @Bean
    public GroupedOpenApi allGroup() {
        return GroupedOpenApi.builder()
                .group("All APIs")
                .pathsToMatch("/**")
                .build();
    }
}
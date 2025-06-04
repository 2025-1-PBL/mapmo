package com.pbl.mapmo.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Mapmo API",
                version = "v1",
                description = "Mapmo REST API 명세서"
        ),
        servers = {
                @Server(url = "/", description = "Default Server URL")
        }
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    /** Swagger UI 좌측 탭에 노출될 기본 그룹(전체) */
    @Bean
    public GroupedOpenApi allApis() {
        return GroupedOpenApi.builder()
                .group("전체 API")
                .pathsToMatch("/**")
                .build();
    }

    /** 추가 메타데이터가 필요하다면 OpenAPI 빈에 정의 */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Mapmo API")
                        .version("v1")
                        .description("Mapmo REST API 명세서")
                        .contact(new Contact()
                                .name("Mapmo Backend Team")
                                .email("support@mapmo.app"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
package com.yjh.imitator.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Chenfei
 */
@Configuration
public class SpringDocConfig {

    /**
     * 基本信息
     */
    private Info info() {
        return new Info().title("voice-imitator Server").description("Intelligent analysis and simulation device for voiceprint Server Open API\nauthor yijiahe").version("v0.0.1")
                .termsOfService("http://www.yijiahe.com/").contact(new Contact().name("yijiahe").email("yijiahe@yijiahe.com"));
    }

    /**
     * 外部文档信息
     */
    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation().description("Voice Imitator Server Open API").url("http://www.yijiahe.com/");
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(info()).externalDocs(externalDocumentation());
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder().group("voice-imitator").packagesToScan("com.yjh.imitator").build();
    }
}
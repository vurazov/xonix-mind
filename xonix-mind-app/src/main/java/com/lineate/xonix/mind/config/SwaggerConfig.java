package com.lineate.xonix.mind.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket getApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lineate.xonix.mind.controller"))
                .paths(PathSelectors.any())
                //.paths(regex("/match.*"))
                .build();

    }

    private ApiInfo getApiInfo() {
        Contact contact = new Contact("Lineate", "https://lineate.com/contact/", "info@lineate.com");
        return new ApiInfoBuilder()
                .title("Xonix REST API")
                .description("REST API for Xonix Game")
                .version("1.0.0")
                .license("Apache License Version 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0\"")
                .contact(contact)
                .build();
    }
}

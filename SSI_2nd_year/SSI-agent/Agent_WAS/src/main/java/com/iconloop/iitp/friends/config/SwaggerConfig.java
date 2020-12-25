package com.iconloop.iitp.friends.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(getApiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.iconloop.iitp.friends.v1.web.controller"))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false)
                .tags(new Tag("Friends", "친구목록 관리"))
                ;

    }

    private ApiInfo getApiInfo() {

        return new ApiInfoBuilder().title("Spring API Documentation")
                .description(getDescription())
                .license(null).licenseUrl(null).version("1").build();

    }

    private String getDescription() {
        String introduction = "SSI - Friends management server API";
        return introduction;
    }
}

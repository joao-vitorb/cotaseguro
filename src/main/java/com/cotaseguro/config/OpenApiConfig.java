package com.cotaseguro.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cotaSeguroOpenApi() {
        Info info = new Info()
                .title("CotaSeguro API")
                .version("v1")
                .description("Insurance quotation and policy issuance REST API");

        return new OpenAPI().info(info);
    }

}

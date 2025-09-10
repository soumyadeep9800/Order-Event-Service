package com.ecommerce.orderevent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI() //http://localhost:8080/swagger-ui/index.html
                .info(new Info()
                        .title("Order-Event-Service API")
                        .version("1.0")
                        .description("API documentation for the Order-Event-Service project"));
    }
}

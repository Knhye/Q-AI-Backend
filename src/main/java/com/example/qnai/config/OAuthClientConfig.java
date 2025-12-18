package com.example.qnai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OAuthClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("https://appleid.apple.com").build();
    }
}

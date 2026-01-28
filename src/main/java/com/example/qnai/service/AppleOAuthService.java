package com.example.qnai.service;

import com.example.qnai.dto.oauth.ApplePublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AppleOAuthService {
    private final WebClient webClient;
    private ApplePublicKeyResponse cachedKeys;

    public AppleOAuthService(@Qualifier("appleWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public ApplePublicKeyResponse getAppleKeys() {
        if (cachedKeys == null) {
            cachedKeys = webClient.get()
                    .uri("/auth/keys")
                    .retrieve()
                    .bodyToMono(ApplePublicKeyResponse.class)
                    .block();
        }
        return cachedKeys;
    }
}

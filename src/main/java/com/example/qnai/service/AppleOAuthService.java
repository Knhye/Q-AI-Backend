package com.example.qnai.service;

import com.example.qnai.dto.oauth.ApplePublicKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AppleOAuthService {
    private final WebClient webClient;
    private ApplePublicKeyResponse cachedKeys;

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

package com.example.qnai.common;

import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AIHealthChecker {

    private final WebClient aiWebClient;

    @Getter
    private String lastErrorMessage; // 마지막 에러 메시지 저장

    public AIHealthChecker(WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    public boolean isHealthy() {
        try {
            ResponseEntity<Void> response = aiWebClient.get()
                    .uri("/")
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                lastErrorMessage = null;
                return true;
            } else {
                lastErrorMessage = "AI 서버 상태 코드 오류: " +
                        (response != null ? response.getStatusCode() : "응답 없음");
                return false;
            }

        } catch (Exception e) {
            lastErrorMessage = e.getMessage(); // AI 서버/네트워크 에러 메시지 저장
            return false;
        }
    }

}


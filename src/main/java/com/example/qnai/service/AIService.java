package com.example.qnai.service;

import com.example.qnai.common.AIHealthChecker;
import com.example.qnai.dto.gpt.request.AIFeedbackRequest;
import com.example.qnai.dto.qna.request.QnaGenerateRequest;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AIService {

    private final AIHealthChecker healthChecker;
    private final WebClient webClient;

    public AIService(@Qualifier("aiWebClient") WebClient webClient, AIHealthChecker healthChecker) {
        this.webClient = webClient;
        this.healthChecker = healthChecker;
    }

    public String generateQuestion(QnaGenerateRequest request){
        healthChecking();


        String response = webClient.post()
                .uri("/ai/qna/question")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if(response == null){
            throw new InternalException("AI로부터 응답을 받지 못했습니다.");
        }

        return response;
    }

    public String generateFeedback(AIFeedbackRequest aiFeedbackRequest){
        healthChecking();

        String feedback = webClient.post()
                .uri("/ai/qna/feedback")
                .bodyValue(aiFeedbackRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if(feedback == null){
            throw new InternalException("AI로부터 응답을 받지 못했습니다.");
        }

        return feedback;
    }

    public void healthChecking() {
        if (!healthChecker.isHealthy()) {
            String msg = healthChecker.getLastErrorMessage();
            if (msg == null || msg.isBlank()) {
                msg = "알 수 없는 오류";
            }
            throw new IllegalStateException("AI 서버 사용 불가: " + msg);
        }
    }

}

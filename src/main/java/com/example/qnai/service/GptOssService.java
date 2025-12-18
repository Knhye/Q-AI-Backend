package com.example.qnai.service;

import com.example.qnai.dto.gpt.response.OpenAIChatResponse;
import com.example.qnai.dto.qna.request.QnaGenerateRequest;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GptOssService {
    @Value("${openai.api-key}")
    private String API_KEY;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1/chat/completions")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public String generateQuestion(QnaGenerateRequest request) {

        String prompt = String.format("""
        You are an expert technical interviewer.
        Generate only one interview question.
        Requirements:
        - Subject: %s
        - Difficulty: %s
        - Output content MUST include only the question text.
        - No explanation, no numbering, no metadata, no JSON.
        - Each question must be separated by a newline.
        - Exactly one line must be returned.
        - Please write the question in Korean.
        """,
                request.getSubject(),
                request.getLevel()
        );

        // OpenAI 요청 바디
        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        OpenAIChatResponse openaiResponse = webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OpenAIChatResponse.class)
                .block();

        if (openaiResponse == null
                || openaiResponse.getChoices() == null
                || openaiResponse.getChoices().isEmpty()
                || openaiResponse.getChoices().getFirst().getMessage() == null
                || openaiResponse.getChoices().getFirst().getMessage().getContent() == null) {

            throw new InternalException("OpenAI 응답이 올바르지 않습니다.");
        }

        // 응답 DTO 구성

        return openaiResponse.getChoices().getFirst().getMessage().getContent();

    }


    public String generateFeedback(String question, String answer) {
        String prompt = String.format("""
        You are an experienced technical interviewer.
        Based on the following question and answer, provide constructive feedback on the answer.

        Requirements:
        - Evaluate how well the answer addresses the technical intent of the question.
        - Provide clear improvement points and guidance from an IT and software engineering perspective.
        - Output must be written in Korean using polite and respectful language.
        - Do not use any images or emoticons. Text only.
        - Write with clarity and high readability.
        - Do not include meta commentary or any content other than the feedback itself.

        [question]
        %s

        [answer]
        %s
        """,
                question,
                answer
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        OpenAIChatResponse openaiResponse = webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OpenAIChatResponse.class)
                .block();

        if (openaiResponse == null
                || openaiResponse.getChoices() == null
                || openaiResponse.getChoices().isEmpty()
                || openaiResponse.getChoices().getFirst().getMessage() == null
                || openaiResponse.getChoices().getFirst().getMessage().getContent() == null) {

            throw new InternalException("OpenAI 응답이 올바르지 않습니다.");
        }

        return openaiResponse.getChoices().getFirst().getMessage().getContent();
    }
}

package com.example.qnai.dto.qna.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackGenerateRequest {
    @NotNull
    private Long qnaId;

    @NotNull
    private String question;

    @NotNull
    private String answer;
}

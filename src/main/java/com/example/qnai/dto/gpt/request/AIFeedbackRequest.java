package com.example.qnai.dto.gpt.request;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AIFeedbackRequest {
    private String question;
    private String answer;
}

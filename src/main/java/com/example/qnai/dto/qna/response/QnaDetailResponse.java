package com.example.qnai.dto.qna.response;

import com.example.qnai.entity.enums.Level;
import com.example.qnai.entity.enums.Subject;
import com.example.qnai.entity.enums.SubjectDetail;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QnaDetailResponse {
    private Long id;
    private String question;
    private String answer;
    private String feedback;
    private Subject subject;
    private SubjectDetail subjectDetail;
    private Level level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

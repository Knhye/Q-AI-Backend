package com.example.qnai.dto.qna.response;

import com.example.qnai.entity.Level;
import com.example.qnai.entity.Subject;
import com.example.qnai.entity.SubjectDetail;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QnaGenerateResponse {
    private Long qnaId;
    private String question;
    private Subject subject;
    private SubjectDetail subjectDetail;
    private Level level;
}

package com.example.qnai.dto.qna.request;

import com.example.qnai.entity.Level;
import com.example.qnai.entity.Subject;
import com.example.qnai.entity.SubjectDetail;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QnaGenerateRequest {
    @NotNull
    private Subject subject;

    @NotNull
    private SubjectDetail subjectDetail;

    @NotNull
    private Level level;
}

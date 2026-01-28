package com.example.qnai.dto.qna.request;

import com.example.qnai.entity.enums.Level;
import com.example.qnai.entity.enums.Subject;
import com.example.qnai.entity.enums.SubjectDetail;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QnaGenerateRequest {
    @NotNull
    private String subject;

    @NotNull
    private String subjectdetail;

    @NotNull
    private String level;
}

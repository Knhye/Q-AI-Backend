package com.example.qnai.dto.notebook.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotebookExcludeItemRequest {
    @NotNull
    private Long notebookId;

    @NotNull
    private Long qnaId;
}

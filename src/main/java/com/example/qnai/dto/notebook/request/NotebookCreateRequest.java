package com.example.qnai.dto.notebook.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotebookCreateRequest {
    @NotNull
    private String name;
}

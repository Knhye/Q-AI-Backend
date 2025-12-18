package com.example.qnai.dto.user.request;

import com.example.qnai.entity.enums.Subject;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @NotNull
    private String email;

    @NotNull
    private String nickname;

    @NotNull
    private Subject mainSubject;
}

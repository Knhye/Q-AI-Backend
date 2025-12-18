package com.example.qnai.dto.user.response;

import com.example.qnai.entity.enums.Subject;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserUpdateResponse {
    private Long userId;
    private String email;
    private String nickname;
    private Subject mainSubject;
    private LocalDateTime createdAt;
}

package com.example.qnai.dto.notification.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class NotificationSettingRequest {
    @NotNull
    private LocalTime preferredTime;
}

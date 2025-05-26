package com.example.smartair.dto.notificationDto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private String message;
    private String title;
    private boolean readStatus;
    private LocalDateTime createdAt;

    public NotificationResponseDto(Long id, String message, String title, boolean readStatus,
                                   LocalDateTime createdAt) {
        this.id = id;
        this.message = message;
        this.title = title;
        this.readStatus = readStatus;
        this.createdAt = createdAt;
    }
}

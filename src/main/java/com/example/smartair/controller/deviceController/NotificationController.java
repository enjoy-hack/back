package com.example.smartair.controller.deviceController;

import com.example.smartair.dto.notificationDto.NotificationResponseDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.deviceService.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();
        List<NotificationResponseDto> notifications = notificationService.getNotifications(user);

        return ResponseEntity.ok(notifications);
    }

    @PutMapping()
    public ResponseEntity<?> markNotificationAsRead(@AuthenticationPrincipal CustomUserDetails userDetails, Long notificationId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok("알림이 읽음 상태로 변경되었습니다.");
    }
}

package com.example.smartair.service.deviceService;

import com.example.smartair.dto.notificationDto.NotificationResponseDto;
import com.example.smartair.entity.notification.Notification;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.notificationRepository.NotificationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponseDto> getNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUser(user);

        return notifications.stream()
                .map(notification -> new NotificationResponseDto(
                        notification.getId(),
                        notification.getMessage(),
                        notification.getTitle(),
                        notification.isReadStatus(),
                        notification.getCreatedAt()))
                .toList();
    }

    public void markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "%s의 알림을 찾을 수 없습니다. " + notificationId));
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

}

package com.example.smartair.repository.notificationRepository;

import com.example.smartair.entity.notification.Notification;
import com.example.smartair.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림을 읽지 않은 상태로 가져오기
    List<Notification> findByUserAndReadStatus(User user, boolean readStatus);

    // 특정 사용자에 대한 모든 알림 가져오기
    List<Notification> findByUser(User user);


}

package com.example.smartair.service.userService;

import com.example.smartair.entity.user.User;
import com.example.smartair.repository.userRepository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LoginService {
    private final UserRepository userRepository;

    public void setFcmToken(User user, String fcmToken) {
        // FCM 토큰을 사용자에게 설정하는 로직
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }
}

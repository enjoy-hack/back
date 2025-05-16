package com.example.smartair.controller.userController;

import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.service.userService.LoginService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login/fcmToken")
    public ResponseEntity<?> setFcmToken(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         String fcmToken) {
        if(fcmToken == null || fcmToken.isEmpty()) {
            return ResponseEntity.badRequest().body("FCM token is required");
        }
        if(userDetails == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        log.info("Received FCM token: " + fcmToken);
        loginService.setFcmToken(userDetails.getUser(), fcmToken);
        return ResponseEntity.ok("FCM token updated successfully");
    }
}

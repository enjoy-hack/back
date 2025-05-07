package com.example.smartair.controller.customUserController;

import com.example.smartair.dto.customUserDto.UserSatisfactionResponseDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.customUserRepository.UserSatisfactionRepository;
import com.example.smartair.service.customUserService.UserSatisfactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserSatisfactionController implements UserSatisfactionControllerDocs{

    private final UserSatisfactionService userSatisfactionService;
    private final UserSatisfactionRepository userSatisfactionRepository;
    // 사용자 만족도 & AQI 별로 set,get 구현 예정
    @PostMapping("/userSatisfaction/{roomId}")
    public ResponseEntity<?> setUsersSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam("satisfaction") Double satisfaction,
                                                  @PathVariable Long roomId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        User user = userDetails.getUser();

        if(user.getRole().equals("ROLE_USER")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("접근 권한이 없습니다");
        }
        userSatisfactionService.setUserSatisfaction(user, satisfaction, roomId);
        return ResponseEntity.ok("sucess");
    }

    @GetMapping("/userSatisfaction/{roomId}")
    public ResponseEntity<?> getUsersSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @PathVariable Long roomId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        User user = userDetails.getUser();

        if(user.getRole().equals("ROLE_USER")){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("접근 권한이 없습니다");
        }
        List<UserSatisfactionResponseDto> list = userSatisfactionService.getUserSatisfaction(user, roomId);
        return ResponseEntity.ok(list);
    }

    // 만족도 수정
    @PutMapping("/userSatisfaction/{satisfactionId}")
    public ResponseEntity<?> updateUserSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @PathVariable Long satisfactionId,
                                                    @RequestParam("newSatisfaction") Double newSatisfaction) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        User user = userDetails.getUser();

        if (user.getRole().equals("ROLE_USER")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("접근 권한이 없습니다");
        }

        try {
            userSatisfactionService.updateUserSatisfaction(user, satisfactionId, newSatisfaction);
            return ResponseEntity.ok("수정 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 만족도 삭제
    @DeleteMapping("/userSatisfaction/{satisfactionId}")
    public ResponseEntity<?> deleteUserSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @PathVariable Long satisfactionId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }

        User user = userDetails.getUser();

        if (user.getRole().equals("ROLE_USER")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("접근 권한이 없습니다");
        }

        try {
            userSatisfactionService.deleteUserSatisfaction(user, satisfactionId);
            return ResponseEntity.ok("삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}

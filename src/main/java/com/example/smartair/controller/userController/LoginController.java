package com.example.smartair.controller.userController;

import com.example.smartair.dto.userDto.LoginDTO;
import com.example.smartair.dto.userDto.TokenDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.service.userService.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그인", description = "로그인 관련 API")
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

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하여 액세스 토큰과 리프레시 토큰을 받습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(
                                    schema = @Schema(implementation = TokenDto.class)
                            )
                    )
            }
    )
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginDTO loginRequestDto) {
        TokenDto tokenDto = loginService.login(loginRequestDto);
        // 응답 본문에 TokenDto를 직접 반환
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(tokenDto);
    }

}

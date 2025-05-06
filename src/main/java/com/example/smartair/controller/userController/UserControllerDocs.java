package com.example.smartair.controller.userController;

import com.example.smartair.dto.userDto.KakaoUserInfoResponseDTO;
import com.example.smartair.dto.userDto.UserInfoDTO;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserControllerDocs {

    @Operation(
            summary = "카카오 로그인 콜백 처리",
            description = """
            ## 카카오 로그인 콜백 처리 
            
            카카오 로그인 후 받은 인가 코드(code)를 이용하여 사용자 인증을 처리하고
            Access Token 및 Refresh Token을 발급합니다.

            ---

            **요청 파라미터**
            - `code` (String): 카카오 OAuth2 서버에서 발급받은 인가 코드

            ---

            **응답 헤더**
            - `access`: Access Token
            - `Set-Cookie`: Refresh Token을 담은 HttpOnly 쿠키

            ---

            **응답 본문**
            - 성공 시: `200 OK`
            - 실패 시: 에러 메시지 포함
            """
    )
    ResponseEntity<?> callback(@RequestParam("code") String code);

    @Operation(
            summary = "사용자 정보 조회",
            description = """
            ## 사용자 정보 조히
            
            로그인된 사용자의 정보를 반환합니다.

            ---

            **요청 정보**
            - 인증 사용자 정보는 `@AuthenticationPrincipal`을 통해 전달됩니다.

            ---

            **응답**
            - 성공 시: `UserInfoDTO 객체` (ID, 이름, 이메일, 권한, 로그인 방식)
            - 실패 시: 인증 오류 메시지
            """
    )
    ResponseEntity<?> getUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails);
}


package com.example.enjoy.controller;

import com.example.enjoy.dto.loginDto.MemberCommand;
import com.example.enjoy.dto.loginDto.MemberDto;
import com.example.enjoy.service.loginService.SejongLoginService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로그인", description = "로그인 관련 API")
@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api/auth/sejong")
public class LoginController {
    private final SejongLoginService sejongLoginService;

    /**
     * 세종대학교 포털 로그인 및 사용자 정보 조회
     * 포털 로그인 -> 고전독서 사이트 SSO 인증 -> 사용자 정보 파싱 및 반환
     */
    @PostMapping("/login")
    public ResponseEntity<MemberDto> loginAndGetUserInfo(@RequestBody @Valid MemberCommand command) {
        try {
            log.info("세종대 포털 로그인 요청: {}", command.getSejongPortalId());
            MemberDto memberInfo = sejongLoginService.login(command);
            log.info("사용자 정보 조회 성공: {}", memberInfo.getStudentName());
            return ResponseEntity.ok(memberInfo);
        } catch (Exception e) {
            log.error("세종대 포털 로그인 및 정보 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("세종대 포털 인증 실패", e);
        }
    }

}

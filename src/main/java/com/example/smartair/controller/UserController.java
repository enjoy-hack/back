package com.example.smartair.controller;

import com.example.smartair.dto.UserDTO.KakaoUserInfoResponseDTO;
import com.example.smartair.entity.login.RefreshEntity;
import com.example.smartair.entity.user.User;
import com.example.smartair.jwt.JWTUtil;
import com.example.smartair.repository.RefreshRepository;
import com.example.smartair.service.UserService.KakaoService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final KakaoService kakaoService;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    @GetMapping("/login/oauth2/kakao")
    public ResponseEntity<?> callback(@RequestParam("code") String code) {
        log.info("kakao code : " + code);
        String accessToken = kakaoService.getAccessTokenFromKakao(code);

        kakaoService.debugUserInfoResponse(accessToken);
        KakaoUserInfoResponseDTO userInfo = kakaoService.getUserInfo(accessToken);

        User user = kakaoService.findUserOrCreateUser(userInfo);

        String access = jwtUtil.createJwt("access", user.getUsername(), String.valueOf(user.getRole()), 600000L);
        String refresh = jwtUtil.createJwt("refresh", user.getUsername(), String.valueOf(user.getRole()), 86400000L);

        addRefreshEntity(user.getUsername(), refresh, 86400000L);
        return ResponseEntity.ok()
                .header("access", access)
                .header("Set-Cookie", createCookie("refresh",refresh).toString())
                .build();
    }
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true); 쿠키 암호화 전송 https://howisitgo1ng.tistory.com/entry/HTTP-Only%EC%99%80-Secure-Cookie
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }


}

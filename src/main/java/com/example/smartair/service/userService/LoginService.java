package com.example.smartair.service.userService;

import com.example.smartair.dto.userDto.LoginDTO;
import com.example.smartair.dto.userDto.TokenDto;
import com.example.smartair.entity.login.RefreshEntity;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.jwt.JWTUtil;
import com.example.smartair.repository.userRepository.RefreshRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import lombok.AllArgsConstructor;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@AllArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    // Access Token 만료 시간: 30분
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 30 * 60 * 1000L;
    // Refresh Token 만료 시간: 7일
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;


    @Transactional
    public TokenDto login(LoginDTO loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtUtil.createJwt(
                "access",
                user.getUsername(),
                user.getRole().toString(),
                user.getEmail(),
                ACCESS_TOKEN_EXPIRE_TIME
        );

        String refreshToken = jwtUtil.createJwt(
                "refresh",
                user.getUsername(),
                user.getRole().toString(),
                user.getEmail(),
                REFRESH_TOKEN_EXPIRE_TIME
        );

        // RefreshEntity 저장
        RefreshEntity refreshEntity = new RefreshEntity();
        refreshEntity.setUsername(user.getUsername());
        refreshEntity.setRefresh(refreshToken);
        refreshEntity.setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME).toString());
        refreshRepository.save(refreshEntity);

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

    }
}

package com.example.smartair.jwt;


import com.example.smartair.dto.userDto.LoginDTO;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.login.RefreshEntity;
import com.example.smartair.repository.userRepository.RefreshRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    //authenticationManager의 역할 : 로그인 정보 추출, 인증 토큰 생성
    private final JWTUtil jwtUtil;

    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException{
        LoginDTO loginDTO;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            loginDTO = objectMapper.readValue(messageBody, LoginDTO.class);
        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청의 형식을 읽을 수 없습니다.", e);
        }

        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();
        System.out.println(email);

        // 토큰은 authenticationManager이 username, password를 검증하기 위해서 발급하는 것
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password, null);
        //검증이 잘 되면 Authentication 반환, 안되면 exception 반환
        return authenticationManager.authenticate(authToken);
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        System.out.println("인증 성공, 토큰 발급할 예정");
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities(); //Collection<GrantedAuthority> 로 하면 GrantedAuthority 타입만 받을 수 있음 (하위 클래스 SimpleGrantedAuthority 허용 X)
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator(); //  SimpleGrantedAuthority에는 ROLE_USER, ROLE_ADMIN이 있어서 ? extends GrantedAuthoroty로 해야됨
        GrantedAuthority auth = iterator.next(); //권한 부여
        String role = auth.getAuthority();
        System.out.println("role 이름: " + role);

        String email = customUserDetails.getEmail();
        String access = jwtUtil.createJwt("access", username, role, email, 600000L);
        String refresh = jwtUtil.createJwt("refresh", username, role, email,86400000L);
        
        addRefreshEntity(username, refresh, 86400000L);
        
        //응답 설정
        response.setHeader("access", access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());

        System.out.println("첫 토큰 나옴");
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
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}

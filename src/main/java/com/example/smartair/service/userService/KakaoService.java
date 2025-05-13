package com.example.smartair.service.userService;

import com.example.smartair.dto.userDto.KakaoTokenResponseDTO;
import com.example.smartair.dto.userDto.KakaoUserInfoResponseDTO;
import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;

import com.example.smartair.repository.userRepository.UserRepository;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@Service
public class KakaoService {
    private String clientId;
    private String clientSecret;
    private final String KAUTH_TOKEN_URL_HOST;
    private final String KAUTH_USER_URL_HOST;

    private final UserRepository userRepository;

    @Autowired
    public KakaoService(@Value("${spring.security.oauth2.client.registration.kakao.client-id}") String clientId,
                        @Value("${spring.security.oauth2.client.registration.kakao.client-secret}") String clientSecret,
                        UserRepository userRepository) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
        this.KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
        this.userRepository = userRepository;
    }

    public String getAccessTokenFromKakao(String code) {
        log.info("getAccessTokenFromKakao 실행");
        KakaoTokenResponseDTO kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", clientId)
                        .queryParam("code", code)
                        .queryParam("client_secret", clientSecret)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
                .bodyToMono(KakaoTokenResponseDTO.class)
                .block();
        log.info("카카오토큰DTO 생성");

        log.info(" [Kakao Service] Access Token ------> {}", kakaoTokenResponseDto.getAccessToken());
        log.info(" [Kakao Service] Refresh Token ------> {}", kakaoTokenResponseDto.getRefreshToken());
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoTokenResponseDto.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDto.getScope());

        return kakaoTokenResponseDto.getAccessToken();
    }

    public void debugUserInfoResponse(String accessToken) {
        String response = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        log.info("[Kakao Service] Raw user info response: {}", response);
    }

    public KakaoUserInfoResponseDTO getUserInfo(String accessToken){
        KakaoUserInfoResponseDTO userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
                .bodyToMono(KakaoUserInfoResponseDTO.class)
                .block();


        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getNickname());
        log.info("[ Kakao Service ] email ---> {} ", userInfo.getEmail());
        return userInfo;
    }

    public User findUserOrCreateUser(KakaoUserInfoResponseDTO userInfo) {
        Optional<User> finduser = userRepository.findByEmail(userInfo.getEmail());
        User user;
        if(finduser.isEmpty()){
            user = new User();
            user.setRole(Role.valueOf("USER"));
            user.setUsername(userInfo.getNickname());
            user.setEmail(userInfo.getEmail());
            user.setLoginType("kakao");
            userRepository.save(user);
        }else{
            user = finduser.get();
        }

        return user;
    }
}

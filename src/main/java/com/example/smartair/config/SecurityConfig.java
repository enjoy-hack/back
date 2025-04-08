package com.example.smartair.config;

import com.example.smartair.jwt.CustomLogoutFilter;
import com.example.smartair.jwt.JWTFilter;
import com.example.smartair.jwt.JWTUtil;
import com.example.smartair.jwt.LoginFilter;
import com.example.smartair.repository.RefreshRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private  final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil, RefreshRepository refreshRepository){
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    } // 중복 생성 없이 어디서든 @Autowired로 호출하고자 Bean 등록
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { //보안 필터를 거치는 모든 요청(CORS + JWT 포함) 처리

        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration configuration = new CorsConfiguration();

                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));// 허용할 메소드 (GET, POST 등)
                        configuration.setAllowCredentials(true); // 모든 요청 헤더 허용
                        configuration.setAllowedHeaders(Collections.singletonList("*")); // 쿠키, 인증 정보 포함 요청 허용
                        configuration.setMaxAge(3600L); // 캐시 시간
                        configuration.setExposedHeaders(Collections.singletonList("Authorization")); // 클라이언트가 Authorization 헤더 확인 허용

                        return configuration;
                    }
                })));

        http
                .csrf((auth) -> auth.disable());

        //From 로그인 방식 disable
        http
                .formLogin((auth) -> auth.disable());

        //http basic 인증 방식 disable
        http
                .httpBasic((auth) -> auth.disable());

        //경로별 인가 작업
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/login", "/", "/join", "/reissue").permitAll()
                        //.requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().permitAll());
        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);
        http // addFilterBefore(새 필터, 기준 필터) 새 필터를 기준 필터 앞에 추가
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
        //기본 로그인 필터 (UsernamePasswordAuthentication) 말고 LoginFilter로 대체
        // LoginFilter은 스프링 컨테이너가 관리하는 Bean이 아니라 독립적 객체라 new 로 선언
        http //addFilterAt(새 필터, 기존 필터) 기존 필터를 새 필터로 교체
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);
        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}


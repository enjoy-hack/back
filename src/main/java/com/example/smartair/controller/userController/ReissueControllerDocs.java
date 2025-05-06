package com.example.smartair.controller.userController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Tag(name = "토큰 재발급 API", description = "Access 및 Refresh 토큰을 재발급하는 API입니다.")
@RequestMapping("/reissue")
public interface ReissueControllerDocs {

    @Operation(
            summary = "Access/Refresh 토큰 재발급",
            description = """
            ## JWT Access 및 Refresh 토큰 재발급

            기존에 발급받은 Refresh 토큰을 기반으로 새로운 Access 토큰과 Refresh 토큰을 발급합니다.

            ---

            **요청 헤더 및 쿠키**
            - 쿠키: `refresh`

            ---

            **처리 로직**
            1. 쿠키에서 Refresh 토큰을 추출
            2. 만료 여부 확인
            3. Refresh 토큰 여부 확인 (카테고리)
            4. DB에 저장된 Refresh 토큰인지 확인
            5. 기존 Refresh 삭제 → 새 토큰 재발급 및 저장
            6. 새 Access 토큰은 헤더에, 새 Refresh 토큰은 쿠키에 반환

            ---

            **응답**
            - 200 OK: `재발급 성공` (헤더에 access, 쿠키에 refresh 포함)
            - 400 BAD_REQUEST: 만료됨, 쿠키 없음, DB에 없음 등 오류
            
            """
    )
    @PostMapping
    ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response);
}

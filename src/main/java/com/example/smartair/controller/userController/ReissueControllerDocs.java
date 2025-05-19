package com.example.smartair.controller.userController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "JWT 토큰 재발급 API", description = "Access Token 재발급 및 Refresh Token Rotation 처리")
public interface ReissueControllerDocs {

    @Operation(
            summary = "JWT 토큰 재발급",
            description = """
            ## 설명
            - 쿠키에 저장된 `refresh` 토큰을 기반으로 Access Token을 재발급합니다.
            - 기존 Refresh 토큰은 폐기되고 새 Refresh + Access 토큰이 함께 발급됩니다.
            - 새 Access는 `access` 헤더에, Refresh는 `Set-Cookie`로 응답됩니다.

            ## 요청
            - `refresh` 토큰은 **HttpOnly 쿠키**로 전송되어야 합니다.
            - 별도의 Request Body는 없습니다.

            ## 응답
            - 성공: 새 access 토큰과 refresh 토큰이 포함됨 (access는 헤더, refresh는 쿠키)
            - 실패: 상태 코드와 에러 메시지 포함된 JSON 반환

            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Access + Refresh 토큰 재발급 성공",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "code": "S001",
                      "message": "New tokens issued"
                    }
                """))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "요청 쿠키에 Refresh 토큰 없음",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "code": "E001",
                      "error": "Refresh token not found in cookies"
                    }
                """))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Refresh 토큰 만료, 위조, DB 없음 등 인증 오류",
                            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "code": "E002",
                      "error": "Refresh token expired"
                    }
                """))
                    )
            }
    )
    ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response);
}
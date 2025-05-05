package com.example.smartair.controller.customUserController;

import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

public interface CustomUserControllerDocs {

    @Operation(
            summary = "사용자의 맞춤 온도 조회",
            description = """
            ## 사용자 맞춤 온도 조회

            현재 로그인된 사용자의 **맞춤 온도(customTemp)** 를 조회합니다.

            ---

            **요청 정보**
            - 인증 정보는 `@AuthenticationPrincipal` 로 자동 전달됩니다.

            ---

            **응답**
            - `200 OK`: 설정된 맞춤 온도 값 반환
            - `401 Unauthorized`: 인증 오류 메시지 반환
            """
    )
    ResponseEntity<?> getCustom(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "사용자의 맞춤 온도 설정",
            description = """
            ## 사용자 맞춤 온도 설정

            로그인된 사용자의 **맞춤 온도(customTemp)** 를 새롭게 저장합니다.

            ---

            **요청 형식**
            - `customTemp` (Double): 설정할 맞춤 온도 값

            ---

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 발생 시 메시지 반환
            """
    )
    ResponseEntity<?> setCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestBody Double customTemp);

    @Operation(
            summary = "사용자의 맞춤 온도 수정",
            description = """
            ## 사용자 맞춤 온도 수정

            로그인된 사용자의 기존 **맞춤 온도(customTemp)** 를 수정합니다.

            ---

            **입력 파라미터**
            - `customTemp` (Double): 수정할 온도 값

            ---

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 시 메시지 반환
            """
    )
    ResponseEntity<?> updateCustomTemp(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody Double customTemp);

    @Operation(
            summary = "사용자의 맞춤 습도 설정",
            description = """
            ## 사용자 맞춤 습도 설정

            로그인된 사용자의 **맞춤 습도(customMoi)** 를 새롭게 저장합니다.

            ---

            **입력 파라미터**
            - `customMoi` (Double): 설정할 습도 값

            ---

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            """
    )
    ResponseEntity<?> setCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestBody Double customMoi);

    @Operation(
            summary = "사용자의 맞춤 습도 수정",
            description = """
            ## 사용자 맞춤 습도 수정

            로그인된 사용자의 기존 **맞춤 습도(customMoi)** 를 수정합니다.

            ---

            **입력 파라미터**
            - `customMoi` (Double): 수정할 습도 값

            ---

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            """
    )
    ResponseEntity<?> updateCustomMoi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody Double customMoi);
}
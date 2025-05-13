package com.example.smartair.controller.customUserController;

import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface CustomUserControllerDocs {

    @Operation(
            summary = "사용자의 맞춤 온도 조회",
            description = """
            ## 사용자 맞춤 온도 조회

            특정 방(Room ID)에 대한 사용자의 맞춤 온도와 습도를 조회합니다.

            ---

            **요청 정보**
            - `roomId` (Long): 조회할 방의 ID
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.

            **응답**
            - `200 OK`: 맞춤 온도 및 습도 값 반환
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우

            **응답 예시**
            ```json
            {
                "temperature": 24.5,
                "moisture": 60.0
            }
            ```
            """
    )
    ResponseEntity<?> getCustom(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "조회할 방의 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(
            summary = "사용자의 맞춤 온도 설정",
            description = """
            ## 사용자 맞춤 온도 설정

            특정 방(Room ID)에 대한 사용자의 맞춤 온도를 새롭게 저장합니다.

            ---

            **요청 정보**
            - `roomId` (Long): 설정할 방의 ID
            - `customTemp` (Double): 설정할 맞춤 온도 값
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우
            """
    )
    ResponseEntity<?> setCustomTemp(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "설정할 맞춤 온도 값") @RequestBody Double customTemp,
            @Parameter(description = "설정할 방의 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(
            summary = "사용자의 맞춤 온도 수정",
            description = """
            ## 사용자 맞춤 온도 수정

            특정 방(Room ID)에 대한 사용자의 기존 맞춤 온도를 수정합니다.

            ---

            **요청 정보**
            - `roomId` (Long): 수정할 방의 ID
            - `customTemp` (Double): 수정할 맞춤 온도 값
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우
            """
    )
    ResponseEntity<?> updateCustomTemp(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 맞춤 온도 값") @RequestBody Double customTemp,
            @Parameter(description = "수정할 방의 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(
            summary = "사용자의 맞춤 습도 설정",
            description = """
            ## 사용자 맞춤 습도 설정

            특정 방(Room ID)에 대한 사용자의 맞춤 습도를 새롭게 저장합니다.

            ---

            **요청 정보**
            - `roomId` (Long): 설정할 방의 ID
            - `customMoi` (Double): 설정할 맞춤 습도 값
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우
            """
    )
    ResponseEntity<?> setCustomMoi(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "설정할 맞춤 습도 값") @RequestBody Double customMoi,
            @Parameter(description = "설정할 방의 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(
            summary = "사용자의 맞춤 습도 수정",
            description = """
            ## 사용자 맞춤 습도 수정

            특정 방(Room ID)에 대한 사용자의 기존 맞춤 습도를 수정합니다.

            ---

            **요청 정보**
            - `roomId` (Long): 수정할 방의 ID
            - `customMoi` (Double): 수정할 맞춤 습도 값
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.

            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우
            """
    )
    ResponseEntity<?> updateCustomMoi(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "수정할 맞춤 습도 값") @RequestBody Double customMoi,
            @Parameter(description = "수정할 방의 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(
            summary = "사용자 맞춤 온도 삭제",
            description = """
            ## 사용자 맞춤 온도 삭제
            
            특정 방(Room ID)에 대한 사용자의 맞춤 온도를 삭제합니다.
            
            ---
            
            **요청 정보**
            - `roomId` (Long): 삭제할 방의 ID
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.
            
            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우
            """
    )
    ResponseEntity<?> deleteCustomTemp(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "삭제할 방의 ID") @PathVariable("roomId") Long roomId
    );

    @Operation(
            summary = "사용자 맞춤 습도 삭제",
            description = """
            ## 사용자 맞춤 습도 삭제
            
            특정 방(Room ID)에 대한 사용자의 맞춤 습도를 삭제합니다.
            
            ---
            
            **요청 정보**
            - `roomId` (Long): 삭제할 방의 ID
            - 인증 정보는 `@AuthenticationPrincipal`로 자동 전달됩니다.
            
            **응답**
            - `200 OK`: "success"
            - `401 Unauthorized`: 인증 오류 메시지 반환
            - `404 Not Found`: 방을 찾을 수 없는 경우
            """
    )
    ResponseEntity<?> deleteCustomMoi(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "삭제할 방의 ID") @PathVariable("roomId") Long roomId
    );
}
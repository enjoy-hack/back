package com.example.smartair.controller.customUserController;

import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserSatisfactionControllerDocs {

    @Operation(
            summary = "사용자 만족도 등록",
            description = """
            ## 사용자 만족도 등록
            로그인된 관리자가 특정 방(roomId)에 대해 사용자 만족도 점수를 등록합니다.

            ---
            
            **요청 파라미터**
            - `satisfaction` (Double): 만족도 점수
            - `roomId` (Long): 방 ID (PathVariable)
            
            ---
            
            **응답**
            - 성공 시: `success`
            - 실패 시: 인증 오류 또는 예외 메시지
            """
    )
    ResponseEntity<?> setUsersSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestParam("satisfaction") Double satisfaction,
                                           @PathVariable Long roomId) throws Exception;

    @Operation(
            summary = "사용자 만족도 조회",
            description = """
            ## 사용자 만족도 조회
            
            특정 방(roomId)에 대해 최근 7개의 사용자 만족도 기록을 조회합니다.

            ---

            **요청 정보**
            - `roomId` (Long): 방 ID (PathVariable)
            - 인증된 사용자 정보는 `@AuthenticationPrincipal`로 전달됨

            ---

            **응답**
            - 성공 시: `UserSatisfactionDto 리스트` 반환
            - 실패 시: 인증 오류 또는 예외 메시지
            """
    )
    ResponseEntity<?> getUsersSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable Long roomId) throws Exception;

    @Operation(
            summary = "사용자 만족도 수정",
            description = """
            ## 사용자 만족도 수정
            
            등록된 사용자 만족도 항목을 수정합니다.

            ---
            
            **요청 파라미터**
            - `satisfactionId` (Long): 만족도 항목의 ID
            - `newSatisfaction` (Double): 새 만족도 값

            ---

            **응답**
            - 성공 시: `수정 성공`
            - 실패 시: 인증 오류 또는 예외 메시지
            """
    )
    ResponseEntity<?> updateUserSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable Long satisfactionId,
                                             @RequestParam("newSatisfaction") Double newSatisfaction);

    @Operation(
            summary = "사용자 만족도 삭제",
            description = """
            ## 사용자 만족도 삭제
    
            등록된 사용자 만족도 항목을 삭제합니다.

            ---
    
            **요청 파라미터**
            - `satisfactionId` (Long): 삭제할 만족도 ID
            
            ---
            
            **응답**
            - 성공 시: `삭제 성공`
            - 실패 시: 인증 오류 또는 예외 메시지
            """
    )
    ResponseEntity<?> deleteUserSatisfaction(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable Long satisfactionId);

}


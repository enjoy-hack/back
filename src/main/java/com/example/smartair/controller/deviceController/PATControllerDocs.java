package com.example.smartair.controller.deviceController;

import com.example.smartair.dto.deviceDto.PATRequestDto;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "PAT API", description = "PAT(Private Access Token) 관리 API")
public interface PATControllerDocs {

    @Operation(
            summary = "PAT 저장",
            description = """
        ## PAT 저장

        사용자의 PAT(Private Access Token)을 암호화하여 저장합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal`을 통해 자동 주입됩니다.

        **요청 본문 (`RequestBody`)**
        - `patToken` (String): 저장할 PAT 토큰
        - `roomId` (Long): PAT와 연결된 방 ID
        - `setting` (Boolean): PAT 설정 (true: 공개키, false: 비공개키/PAT 주인만 사용)

        ---

        **응답**
        - `200 OK`: PAT 저장 성공 메시지 반환
        - `400 Bad Request`: PAT 토큰이 유효하지 않을 경우 오류 메시지 반환
        - `401 Unauthorized`: 인증 정보가 없을 경우 오류 메시지 반환
        - `500 Internal Server Error`: 암호화 실패 시 오류 메시지 반환
        """
    )
    ResponseEntity<?> savePAT(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestBody PATRequestDto request);

//    @Operation(
//            summary = "PAT 설정 변경",
//            description = """
//        ## PAT 설정 변경
//
//        사용자의 PAT 설정을 변경합니다.
//        - 공개키로 설정하면 다른 사용자도 해당 PAT를 사용할 수 있습니다.
//        - 비공개키로 설정하면 PAT 주인만 사용할 수 있습니다.
//        - PAT 설정은 PAT 주인만 직접 변경할 수 있습니다.
//        - PAT의 현재 설정과 반대로 변경됩니다.
//        ---
//
//        **요청 정보**
//        - 인증 정보는 `@AuthenticationPrincipal`을 통해 자동 주입됩니다.
//
//        ---
//
//        **응답**
//        - `200 OK`: PAT 설정 변경 성공 메시지 반환
//        - `400 Bad Request`: PAT 토큰이 존재하지 않을 경우 오류 메시지 반환
//        - `401 Unauthorized`: 인증 정보가 없을 경우 오류 메시지 반환
//        - `500 Internal Server Error`: 설정 변경 실패 시 오류 메시지 반환
//        """
//    )
//    ResponseEntity<?> updatePATSetting(@AuthenticationPrincipal CustomUserDetails userDetails);
}


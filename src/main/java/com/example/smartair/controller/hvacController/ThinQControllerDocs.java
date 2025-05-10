package com.example.smartair.controller.hvacController;

import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "ThinQ API", description = "LG ThinQ 디바이스 제어 및 상태 조회 API")
public interface ThinQControllerDocs {

    @Operation(
            summary = "ThinQ 디바이스 목록 조회",
            description = """
        ## ThinQ 디바이스 목록 조회

        현재 로그인된 사용자의 LG ThinQ 연동 디바이스 목록을 조회합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        ---

        **응답**
        - `200 OK`: 디바이스 목록 JSON 문자열
        - `401 Unauthorized`: 인증 실패
        - `400 Bad Request`: PAT 토큰 미등록 시 오류 메시지
        """
    )
    ResponseEntity<String> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception;

    @Operation(
            summary = "디바이스 상태 조회",
            description = """
        ## 특정 디바이스 상태 조회

        주어진 디바이스 ID에 해당하는 LG ThinQ 디바이스의 상태를 조회합니다.

        ---

        **요청 파라미터**
        - `deviceId` (String): 상태를 조회할 디바이스의 고유 ID

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        ---

        **응답**
        - `200 OK`: 디바이스 상태 JSON 문자열
        - `401 Unauthorized`: 인증 실패
        - `400 Bad Request`: PAT 토큰 미등록 또는 디바이스 ID 오류
        """
    )
    ResponseEntity<String> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("deviceId") String deviceId) throws Exception;

    @Operation(
            summary = "공기청정기 전원 토글 제어",
            description = """
        ## 공기청정기 전원 제어

        공기청정기의 전원을 현재 상태에 따라 토글합니다.
        - 켜져 있으면 끄고, 꺼져 있으면 켭니다.

        ---

        **요청 파라미터**
        - `deviceId` (String): 제어할 디바이스의 고유 ID

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        ---

        **응답**
        - `200 OK`: 제어 성공 메시지 또는 응답 본문
        - `401 Unauthorized`: 인증 실패
        - `400 Bad Request`: PAT 토큰 미등록 또는 디바이스 제어 실패
        """
    )
    ResponseEntity<String> controlPower(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("deviceId") String deviceId) throws Exception;
}

package com.example.smartair.controller.deviceController;

import com.example.smartair.dto.deviceDto.DeviceReqeustDto;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "LG ThinQ 연동 API", description = "LG ThinQ 기반 공기청정기 및 IoT 디바이스 제어 기능 제공")
public interface ThinQControllerDocs {

    @Operation(
            summary = "방 ID를 통해 디바이스 목록 조회",
            description = """
        ## 방 ID를 통해 디바이스 목록 조회

        사용자가 특정 방(roomId)에 연결된 디바이스 목록을 조회합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal`을 통해 자동 주입됩니다.
        - 요청 본문에는 `roomId`가 포함되어야 합니다.

        **요청 본문 (`RequestBody`)**
        ```json
        {
            "roomId": 123
        }
        ```

        ---

        **응답**
        - `200 OK`: 디바이스 목록 조회 성공
        - `401 Unauthorized`: 인증 정보가 없거나 유효하지 않은 경우
        - `500 Internal Server Error`: 서버 내부 오류 발생 시

        **응답 예시**
        ```json
        [
            {
                "userId": 1,
                "roomId": 123,
                "deviceId": "device123",
                "deviceType": "AirPurifier",
                "modelName": "LG1234",
                "alias": "거실 공기청정기"
            },
            {
                "userId": 1,
                "roomId": 123,
                "deviceId": "device456",
                "deviceType": "AirConditioner",
                "modelName": "LG5678",
                "alias": "침실 에어컨"
            }
        ]
        ```
        """
    )
    ResponseEntity<String> getDevices(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "디바이스 목록 조회 요청 정보") @RequestBody DeviceReqeustDto.getDeviceListDto getDeviceListDto
    ) throws Exception;

    @Operation(
            summary = "특정 디바이스의 상태 조회",
            description = """
        ## 특정 디바이스의 상태 조회

        디바이스 ID를 통해 특정 디바이스의 상태를 조회합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal`을 통해 자동 주입됩니다.
        - 요청 본문에는 `deviceId`와 `roomId`가 포함되어야 합니다.

        **요청 본문 (`RequestBody`)**
        ```json
        {
            "deviceId": "device123",
            "roomId": 123
        }
        ```

        ---

        **응답**
        - `200 OK`: 디바이스 상태 조회 성공
        - `401 Unauthorized`: 인증 정보가 없거나 유효하지 않은 경우
        - `404 Not Found`: 디바이스를 찾을 수 없는 경우
        - `500 Internal Server Error`: 서버 내부 오류 발생 시
        """
    )
    ResponseEntity<String> getDeviceStatus(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "디바이스 상태 조회 요청 정보") @RequestBody DeviceReqeustDto.deviceRequestDto deviceRequestDto
    )throws Exception;

    @Operation(
            summary = "공기청정기 전원 제어",
            description = """
        ## 공기청정기 전원 제어

        특정 디바이스의 전원을 켜거나 끕니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal`을 통해 자동 주입됩니다.
        - 요청 본문에는 `deviceId`와 `roomId` 상태가 포함되어야 합니다.

        **요청 본문 (`RequestBody`)**
        ```json
        {
            "deviceId": "device123",
            "roomId": 123
        }
        ```

        ---

        **응답**
        - `200 OK`: 전원 제어 성공
        - `401 Unauthorized`: 인증 정보가 없거나 유효하지 않은 경우
        - `404 Not Found`: 디바이스를 찾을 수 없는 경우
        - `500 Internal Server Error`: 서버 내부 오류 발생 시

        """
    )
    ResponseEntity<String> controlPower(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "디바이스 전원 제어 요청 정보") @RequestBody DeviceReqeustDto.deviceRequestDto deviceRequestDto
    ) throws Exception;
}
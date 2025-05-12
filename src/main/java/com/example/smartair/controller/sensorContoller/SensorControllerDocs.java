package com.example.smartair.controller.sensorContoller;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Sensor API", description = "사용자 센서 관리 API")
public interface SensorControllerDocs {

    @Operation(
            summary = "디바이스 등록",
            description = """
        ## 디바이스 등록

        사용자가 새로운 디바이스를 등록합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        **요청 본문 (`RequestBody`)**
        - `deviceDto` (Object): 디바이스 등록에 필요한 정보

        ---

        **응답**
        - `200 OK`: "success" 메시지 반환
        - `401 Unauthorized`: 인증 정보가 없을 경우 오류 메시지 반환
        """
    )
    ResponseEntity<?> setSensor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestBody SensorRequestDto.setSensorDto deviceDto) throws Exception;

    @Operation(
            summary = "디바이스 삭제",
            description = """
        ## 디바이스 삭제

        사용자가 등록한 디바이스를 삭제합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        **요청 본문 (`RequestBody`)**
        - `deviceDto` (Object): 삭제할 디바이스 정보

        ---

        **응답**
        - `200 OK`: "success" 메시지 반환
        - `401 Unauthorized`: 인증 정보가 없을 경우 오류 메시지 반환
        """
    )
    ResponseEntity<?> deleteSensor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestBody SensorRequestDto.deleteSensorDto deviceDto) throws Exception;

    @Operation(
            summary = "디바이스 목록 조회",
            description = """
        ## 디바이스 목록 조회

        사용자가 등록한 디바이스 목록을 조회합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        **요청 본문 (`RequestBody`)**
        - `roomId` (Long): 조회할 방 ID

        ---

        **응답**
        - `200 OK`: 디바이스 목록 반환
        - `401 Unauthorized`: 인증 정보가 없을 경우 오류 메시지 반환
        """
    )
    ResponseEntity<String> getSensors(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @RequestBody Long roomId);

    @Operation(
            summary = "디바이스 상태 조회",
            description = """
        ## 디바이스 상태 조회

        특정 디바이스의 상태를 조회합니다.

        ---

        **요청 정보**
        - 인증 정보는 `@AuthenticationPrincipal` 을 통해 자동 주입됩니다.

        **요청 본문 (`RequestBody`)**
        - `deviceSerialNumber` (Long): 조회할 디바이스의 일련번호

        ---

        **응답**
        - `200 OK`: 디바이스 상태 반환
        - `401 Unauthorized`: 인증 정보가 없을 경우 오류 메시지 반환
        """
    )
    ResponseEntity<?> getSensorStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @RequestBody Long deviceSerialNumber) throws Exception;
}
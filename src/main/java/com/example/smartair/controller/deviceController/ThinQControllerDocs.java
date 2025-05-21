package com.example.smartair.controller.deviceController;

import com.example.smartair.dto.deviceDto.DeviceReqeustDto;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
@Tag(name = "LG ThinQ", description = "LG ThinQ 연동 디바이스 제어 및 조회 API")
public interface ThinQControllerDocs {

    @Operation(
            summary = "디바이스 목록 조회",
            description = "해당 방 ID에 등록된 LG ThinQ 연동 디바이스 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "deviceId", description = "방 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "디바이스 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    [
                        {
                            "deviceId": 1,
                            "alias": "에어로타워"
                        },
                        {
                            "deviceId": 2,
                            "alias": "스틱청소기"
                        }
                    ]
                    """)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 없음 또는 유효하지 않음)"),
                    @ApiResponse(responseCode = "403", description = "해당 방에 대한 접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "해당 방 또는 PAT 정보 없음")
            }
    )
    ResponseEntity<?> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    @PathVariable("roomId") Long roomId) throws Exception;

    @Operation(
            summary = "디바이스 방 업데이트",
            description = "지정된 디바이스를 새로운 방으로 이동시킵니다.\n" +
                    "사용자는 디바이스의 현재 방과 이동할 방에 대해서 수정할 권한이 있어야 합니다.\n",
            parameters = {
                    @Parameter(name = "deviceId", description = "디바이스 ID", required = true, example = "1"),
                    @Parameter(name = "roomId", description = "방 ID", required = true, example = "2")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "디바이스 방 업데이트 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                {
                    "id": 1,
                    "alias": "에어컨",
                    "roomId": 2
                }
                """)
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "방 또는 디바이스를 찾을 수 없음")
            }
    )
    ResponseEntity<?> updateDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("roomId") Long roomId,
                                           @PathVariable("deviceId") Long deviceId) throws Exception;
    @Operation(
            summary = "디바이스 상태 조회",
            description = "지정된 디바이스 ID의 상세 상태 정보를 반환합니다.",
            parameters = {
                    @Parameter(name = "deviceId", description = "디바이스 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "디바이스 상태 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "messageId": "NWIwYWY5MjQtYzc0MC00Zj",
                        "timestamp": "2025-05-20T05:35:29.751005",
                        "response": {
                            "airFanJobMode": {
                                "currentJobMode": "SPACE_CLEAN"
                            },
                            "operation": {
                                "airFanOperationMode": "POWER_ON"
                            },
                            "timer": {
                                "absoluteStartTimer": "UNSET",
                                "absoluteStopTimer": "UNSET"
                            },
                            "sleepTimer": {
                                "relativeStopTimer": "UNSET"
                            },
                            "airFlow": {
                                "windStrength": "WIND_4",
                                "windTemperature": 30,
                                "windAngle": "OFF",
                                "warmMode": "WARM_OFF"
                            },
                            "airQualitySensor": {
                                "odor": 1,
                                "odorLevel": "WEAK",
                                "PM1": 10,
                                "PM2": 14,
                                "PM10": 19,
                                "humidity": 76,
                                "temperature": 25.5,
                                "totalPollution": 1,
                                "totalPollutionLevel": "GOOD",
                                "monitoringEnabled": "ON_WORKING"
                            },
                            "display": {
                                "light": "LEVEL_3"
                            }
                        }
                    }
                    """)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "디바이스 또는 상태 정보 없음"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음")
            }
    )
    ResponseEntity<?> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable("deviceId") Long deviceId) throws Exception;

    @Operation(
            summary = "공기청정기 전원 제어",
            description = "지정된 디바이스의 전원을 ON ↔ OFF로 전환합니다.",
            parameters = {
                    @Parameter(name = "deviceId", description = "디바이스 ID", required = true, example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "제어 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(value = """
                    {
                        "headers": {
                            "Date": [
                                "Tue, 20 May 2025 05:35:57 GMT"
                            ],
                            "Content-Type": [
                                "application/json"
                            ],
                            "Content-Length": [
                                "93"
                            ],
                            "Connection": [
                                "keep-alive"
                            ],
                            "server": [
                                "istio-envoy"
                            ],
                            "vary": [
                                "Origin"
                            ],
                            "x-envoy-upstream-service-time": [
                                "606"
                            ],
                            "x-krakend": [
                                "Version 2.7.5-ee"
                            ],
                            "x-krakend-completed": [
                                "false"
                            ],
                            "x-envoy-decorator-operation": [
                                "openapi-apigw.ns-connect.svc.cluster.local:8080/*"
                            ]
                        },
                        "body": "{\\\"messageId\\\":\\\"YWI5YTcyMjktOWZhMi00ZW\\\",\\\"timestamp\\\":\\\"2025-05-20T05:35:57.172966\\\",\\\"response\\\":{}}",
                        "statusCode": "OK",
                        "statusCodeValue": 200
                    }
                    """)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "디바이스 정보 없음"),
                    @ApiResponse(responseCode = "500", description = "제어 실패 또는 ThinQ API 오류")
            }
    )
    ResponseEntity<?> controlPower(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @PathVariable("deviceId") Long deviceId) throws Exception;
}
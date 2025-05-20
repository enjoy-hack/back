package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface MqttReceiveControllerDocs {
    @Operation(
            summary = "MQTT 메시지 수신 및 처리",
            description = """
                    **MQTT 메시지 수신 및 센서 자동 등록**

                    MQTT 메시지를 JSON 형식으로 전달받아 처리하고, 관련 데이터를 데이터베이스에 저장합니다.
                    요청 본문은 `topic`과 `payload` 객체를 포함해야 합니다.
                    센서에서 전송된 MQTT 메시지를 처리
                    
                    **토픽 구조**
                    - 형식: smartair/{serialNumber}/airquality
                    - serialNumber: 센서의 고유 일련번호

                    **메시지 처리 제한**
                    - 센서별 시간당 메시지 제한 적용
                    - 제한 초과 시 MQTT_RATE_LIMIT_EXCEEDED 오류 발생

                    **입력 형식 예시:**
                    ```json
                    {
                      "topic": "smartair/1/airQuality",
                      "payload": {
                        "pt1": {
                          "pm10_standard": 22,
                          "pm25_standard": 37,
                          "pm100_standard": 39,
                          "particles_03um": 3867,
                          "particles_05um": 1132,
                          "particles_10um": 283,
                          "particles_25um": 15,
                          "particles_50um": 2,
                          "particles_100um": 1
                        },
                        "pt2": {
                          "pm10_standard": 18,
                          "pm25_standard": 29,
                          "pm100_standard": 36,
                          "particles_03um": 3468,
                          "particles_05um": 940,
                          "particles_10um": 191,
                          "particles_25um": 22,
                          "particles_50um": 10,
                          "particles_100um": 4
                        },
                        "temperature": 29.47,
                        "hum": 38.22,
                        "pressure": 399,
                        "tvoc": 6,
                        "eco2": 400,
                        "rawh2": 12656,
                        "rawethanol": 1647
                      }
                    }
                    ```
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "580", description = "잘못된 토픽 형식"),
            @ApiResponse(responseCode = "581", description = "센서를 찾을 수 없음"),
            @ApiResponse(responseCode = "582", description = "센서 메시지 제한 초과"),
            @ApiResponse(responseCode = "583", description = "MQTT 데이터 파싱 오류"),
            @ApiResponse(responseCode = "584", description = "서비스 처리 오류")
    })
    @PostMapping
    ResponseEntity<?> receiveMqttMessage(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MqttMessageRequestDto requestDto);
}

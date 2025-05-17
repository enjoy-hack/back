package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface MqttReceiveControllerDocs {
    @Operation(
            summary = "MQTT 메시지 수신 및 처리",
            description = """
                    **MQTT 메시지 수신 및 처리**

                    MQTT 메시지를 JSON 형식으로 전달받아 처리하고, 관련 데이터를 데이터베이스에 저장합니다.
                    요청 본문은 `topic`과 `payload` 객체를 포함해야 합니다.

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
            @ApiResponse(responseCode = "200", description = "메시지 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<?> receiveMqttMessage(@RequestBody MqttMessageRequestDto requestDto);
}

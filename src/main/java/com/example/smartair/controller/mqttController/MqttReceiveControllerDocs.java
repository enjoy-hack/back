package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import io.swagger.v3.oas.annotations.Operation;
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

                    **입력 파라미터 (`RequestBody`)**
                    - `topic` (String): 메시지가 발행된 MQTT 토픽 문자열입니다. 일반적으로 `/`로 구분된 계층 구조를 가지며, 메시지 출처(디바이스 ID, 방 ID)를 식별하는 데 사용됩니다. (예: `smartair/1/1/airquality`)
                    - `payload` (String): 측정된 공기질 센서 데이터를 나타내는 JSON 형식의 문자열입니다. 온도, 습도, 미세먼지 농도, 입자 개수 등 다양한 측정값을 포함합니다.

                    **반환값**
                    - 성공 시 (HTTP 200 OK): 메시지 처리 완료를 나타내는 확인 문자열입니다. (예: "테스트 메시지 처리 완료 ...") 
                    - 실패 시: 오류 코드 및 메시지를 포함하는 JSON 응답입니다.
                    """
    )
    ResponseEntity<String> receiveMqttMessage(@RequestBody MqttMessageRequestDto requestDto);
}

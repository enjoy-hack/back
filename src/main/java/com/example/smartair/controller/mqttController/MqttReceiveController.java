package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.service.mqttService.MqttReceiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mqtt/receive")
public class MqttReceiveController implements MqttReceiveControllerDocs{

    private final MqttReceiveService mqttReceiveService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttReceiveController(MqttReceiveService mqttReceiveService) {
        this.mqttReceiveService = mqttReceiveService;
    }

    @GetMapping("/recent")
    public List<SensorAirQualityData> getRecentMessage(){
        return mqttReceiveService.getRecentMessage();
    }

    @PostMapping
    public ResponseEntity<String> receiveMqttMessage(@RequestBody MqttMessageRequestDto requestDto) {
        try {
            AirQualityPayloadDto dto = mqttReceiveService.handleReceiveMessage(
                    requestDto.getTopic(),
                    objectMapper.writeValueAsString(requestDto.getPayload())
                    );
            return ResponseEntity.ok("테스트 메시지 처리 완료 " + dto);

        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("메시지 처리 중 오류 발생: " + e.getMessage());
        }
    }
}

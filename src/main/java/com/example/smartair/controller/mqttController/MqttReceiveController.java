package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.service.MqttService.MqttReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/mqtt/receive")
public class MqttReceiveController implements MqttReceiveControllerDocs{

    private final MqttReceiveService mqttReceiveService;

    public MqttReceiveController(MqttReceiveService mqttReceiveService) {
        this.mqttReceiveService = mqttReceiveService;
    }

    @GetMapping("/recent")
    public List<DeviceAirQualityData> getRecentMessage(){
        return mqttReceiveService.getRecentMessage();
    }

    @PostMapping
    public ResponseEntity<String> receiveMqttMessage(@RequestBody MqttMessageRequestDto requestDto) {
        AirQualityPayloadDto dto = mqttReceiveService.handleReceiveMessage(requestDto.getTopic(), requestDto.getPayload());
        return ResponseEntity.ok("테스트 메시지 처리 완료 " + dto);
    }
}

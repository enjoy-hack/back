package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.service.mqttService.MqttReceiveService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/mqtt/receive")
public class MqttReceiveController implements MqttReceiveControllerDocs{

    private final MqttReceiveService mqttReceiveService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttReceiveController(MqttReceiveService mqttReceiveService) {
        this.mqttReceiveService = mqttReceiveService;
    }


    @Override
    @PostMapping
    public ResponseEntity<?> receiveMqttMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MqttMessageRequestDto requestDto) {

        if (userDetails == null) {
            return buildErrorResponse( "인증 정보가 없습니다", 582);
        }

        try {
            AirQualityPayloadDto dto = mqttReceiveService.handleReceiveMessage(
                    requestDto.getTopic(),
                    objectMapper.writeValueAsString(requestDto.getPayload())
            );
            return ResponseEntity.ok("테스트 메시지 처리 완료 " + dto);

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류", e);
            return buildErrorResponse( e.getMessage(), 583);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 토픽 형식", e);
            return buildErrorResponse("유효하지 않은 MQTT 토픽 형식입니다: " + e.getMessage(), 580);
        } catch (EntityNotFoundException e) {
            log.error("센서를 찾을 수 없음", e);
            return buildErrorResponse("센서를 찾을 수 없습니다: " + e.getMessage(), 581);
        } catch (IllegalStateException e){
            log.error("과도한 요청", e);
            return buildErrorResponse("과도한 요청입니다: " + e.getMessage(), 582);
        } catch (Exception e) {
            log.error("예상치 못한 오류", e);
            return buildErrorResponse("처리 중 오류가 발생했습니다: " + e.getMessage(), 584);
        }
    }

    private ResponseEntity<Map<String, String>> buildErrorResponse(String message, int status) {
        return ResponseEntity.status(status).body(Map.of(
                "error", message,
                "status", String.valueOf(status)

        ));
    }

}

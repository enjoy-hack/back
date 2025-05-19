package com.example.smartair.controller.mqttController;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.dto.mqttMessageDto.MqttMessageRequestDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.service.mqttService.MqttReceiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @Operation(
            summary = "MQTT 메시지 수신 및 처리",
            description = "센서에서 수신한 MQTT 메시지를 JSON으로 처리합니다..."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "MQTT 데이터 파싱 오류"),
            @ApiResponse(responseCode = "429", description = "시간당 메시지 제한 초과"),
            @ApiResponse(responseCode = "503", description = "서비스 처리 오류")
    })
    @Override
    @PostMapping
    public ResponseEntity<String> receiveMqttMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MqttMessageRequestDto requestDto) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        try {
            AirQualityPayloadDto dto = mqttReceiveService.handleReceiveMessage(
                    requestDto.getTopic(),
                    objectMapper.writeValueAsString(requestDto.getPayload())
            );
            return ResponseEntity.ok("테스트 메시지 처리 완료 " + dto);

        } catch (ResponseStatusException e) {
            // ResponseStatusException이 던져지면 HTTP 상태 코드와 메시지를 그대로 반환
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());

        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("메시지 처리 중 서버 오류 발생: " + e.getMessage());
        }
    }

}

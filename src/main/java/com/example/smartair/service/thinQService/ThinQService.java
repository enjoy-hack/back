package com.example.smartair.service.thinQService;

import com.example.smartair.dto.thinQDto.DeviceStateResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class ThinQService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${thinq.api.base-url}")
    private String baseUrl;

    @Value("${thinq.api.api-key}")
    private String apiKey;

    @Value("${thinq.api.country}")
    private String country;

    @Value("${thinq.api.client-id-prefix}")
    private String clientIdPrefix;

    @Value("${thinq.api.pat}")
    private String patToken;

    private final String clientId = clientIdPrefix + UUID.randomUUID();

    public ThinQService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<String> getDeviceList() {
        return sendRequest("/devices", HttpMethod.GET, null);
    }

    public ResponseEntity<String> getDeviceStatus(String deviceId) {
        return sendRequest("/devices/" + deviceId + "/state", HttpMethod.GET, null);
    }

    public ResponseEntity<String> controlAirPurifierPower(String deviceId) throws JsonProcessingException {
        // 디바이스 상태 저장
        DeviceStateResponseDto state = objectMapper.readValue(getDeviceStatus(deviceId).getBody(), DeviceStateResponseDto.class);

        // 켜져있으면 끄고, 꺼져있으면 키고
        String currentPowerMode = state.getResponse().getOperation().getAirFanOperationMode();
        String newMode = currentPowerMode.equals("POWER_ON") ? "POWER_OFF" : "POWER_ON";

        // 요청 본문 생성
        Map<String, Object> requestBody = Map.of(
                "operation", Map.of("airFanOperationMode", newMode)
        );

        // 요청 전송
        return sendRequest("/devices/" + deviceId + "/control", HttpMethod.POST, requestBody);
    }

    // 공통 요청 메서드
    private ResponseEntity<String> sendRequest(String endpoint, HttpMethod method, Object body) {
        HttpHeaders headers = generateHeaders();
        HttpEntity<Object> request = new HttpEntity<>(body, headers); // 요청 본문과 헤더 설정
        return restTemplate.exchange(baseUrl + endpoint, method, request, String.class); // 요청 전송
    }

    private HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-message-id", generateMessageId());
        headers.set("x-country", country);
        headers.set("x-api-key", apiKey);
        headers.set("x-client-id", clientId);
        headers.set("Authorization", "Bearer " + patToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String generateMessageId() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes())
                .substring(0, 22);
    }
}
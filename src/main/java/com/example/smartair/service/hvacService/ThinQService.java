package com.example.smartair.service.hvacService;

import com.example.smartair.dto.hvacDto.DeviceStateResponseDto;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.hvacRepository.PATRepository;
import com.example.smartair.util.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class ThinQService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PATRepository patRepository;
    private final EncryptionUtil encryptionUtil;

    @Value("${thinq.api.base-url}")
    private String baseUrl;

    @Value("${thinq.api.api-key}")
    private String apiKey;

    @Value("${thinq.api.country}")
    private String country;

    @Value("${thinq.api.client-id-prefix}")
    private String clientIdPrefix;

    private final String clientId;

    public ThinQService(RestTemplate restTemplate, ObjectMapper objectMapper, PATRepository patRepository,
                        EncryptionUtil encryptionUtil) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.patRepository = patRepository;
        this.clientId = clientIdPrefix + UUID.randomUUID();
        this.encryptionUtil = encryptionUtil;
    }

    /** 사용자 디바이스 목록 조회 */
    public ResponseEntity<String> getDeviceList(User user) throws Exception {
        String patToken = getDecryptedToken(user);
        return sendRequest("/devices", HttpMethod.GET, null, patToken);
    }

    /** 특정 디바이스 상태 조회 */
    public ResponseEntity<String> getDeviceStatus(User user, String deviceId) throws Exception {
        String patToken = getDecryptedToken(user);
        return sendRequest("/devices/" + deviceId + "/state", HttpMethod.GET, null, patToken);
    }

    /** 공기청정기 전원 제어 */
    public ResponseEntity<String> controlAirPurifierPower(User user, String deviceId) throws Exception {
        String patToken = getDecryptedToken(user);

        ResponseEntity<String> deviceStatusResponse = getDeviceStatus(user, deviceId);
        if (!deviceStatusResponse.getStatusCode().is2xxSuccessful()) {
            log.warn("디바이스 상태를 가져오는 데 실패했습니다. 응답: {}", deviceStatusResponse);
            throw new IllegalStateException("디바이스 상태 조회 실패");
        }

        DeviceStateResponseDto state = objectMapper.readValue(deviceStatusResponse.getBody(), DeviceStateResponseDto.class);
        String currentMode = state.getResponse().getOperation().getAirFanOperationMode();
        String newMode = currentMode.equals("POWER_ON") ? "POWER_OFF" : "POWER_ON";

        Map<String, Object> requestBody = Map.of(
                "operation", Map.of("airFanOperationMode", newMode)
        );

        return sendRequest("/devices/" + deviceId + "/control", HttpMethod.POST, requestBody, patToken);
    }

    /** 사용자 PAT 복호화 */
    private String getDecryptedToken(User user) throws Exception {
        return patRepository.findByUserId(user.getId())
                .map(pat -> {
                    try {
                        return encryptionUtil.decrypt(pat.getEncryptedPat());
                    } catch (Exception e) {
                        throw new RuntimeException("PAT 복호화 실패", e);
                    }
                })//
                .orElseThrow(() -> new RuntimeException("PAT 토큰을 찾을 수 없습니다. 사용자 ID: " + user.getId()));
    }

    /** 공통 API 요청 처리 */
    private ResponseEntity<String> sendRequest(String endpoint, HttpMethod method, Object body, String patToken) {
        HttpHeaders headers = generateHeaders(patToken);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        String url = baseUrl + endpoint;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("LG ThinQ API 실패: 상태 코드={}, 응답={}", response.getStatusCode(), response.getBody());
            }
            return response;
        } catch (Exception e) {
            log.error("ThinQ API 요청 실패: [{} {}] {}", method, url, e.getMessage(), e);
            throw new RuntimeException("LG ThinQ API 요청 중 오류 발생", e);
        }
    }

    /** 요청 헤더 생성 */
    private HttpHeaders generateHeaders(String patToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-message-id", generateMessageId());
        headers.set("x-country", country);
        headers.set("x-api-key", apiKey);
        headers.set("x-client-id", clientId);
        headers.set("Authorization", "Bearer " + patToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /** 메시지 ID 생성 */
    private String generateMessageId() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes())
                .substring(0, 22);
    }
}

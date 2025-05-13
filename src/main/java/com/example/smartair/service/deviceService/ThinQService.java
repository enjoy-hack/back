package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceReqeustDto;
import com.example.smartair.dto.deviceDto.DeviceResponseWrapper;
import com.example.smartair.dto.deviceDto.DeviceStateResponseDto;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.device.PATEntity;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.deviceRepository.PATRepository;
import com.example.smartair.repository.roomParticipantRepository.RoomParticipantRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.util.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThinQService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PATRepository patRepository;
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final EncryptionUtil encryptionUtil;

    private final String baseUrl;
    private final String apiKey;
    private final String country;
    private final String clientId;

    public ThinQService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            PATRepository patRepository,
            EncryptionUtil encryptionUtil,
            DeviceRepository deviceRepository,
            RoomRepository roomRepository,
            RoomParticipantRepository roomParticipantRepository,
            @Value("${thinq.api.base-url}") String baseUrl,
            @Value("${thinq.api.api-key}") String apiKey,
            @Value("${thinq.api.country}") String country,
            @Value("${thinq.api.client-id-prefix}") String clientIdPrefix
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.patRepository = patRepository;
        this.encryptionUtil = encryptionUtil;
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.roomParticipantRepository = roomParticipantRepository;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.country = country;
        this.clientId = clientIdPrefix + UUID.randomUUID();
    }

    public ResponseEntity<String> getDeviceList(User user, Long roomId) throws Exception {
        PATEntity patEntity = getPatEntityOrThrow(user, roomId);
        String patToken = decryptPatToken(patEntity);

        String responseJson = sendRequest("/devices", HttpMethod.GET, null, patToken).getBody();
        DeviceResponseWrapper wrapper = objectMapper.readValue(responseJson, DeviceResponseWrapper.class);

        List<Device> settings = wrapper.getResponse().stream()
                .map(device -> toHvacSetting(user.getId(), roomId, device))
                .collect(Collectors.toList());

        deviceRepository.saveAll(settings);
        log.info("사용자 ID {}의 방 {}에 대해 {}개의 디바이스를 저장했습니다.", user.getId(), roomId, settings.size());

        return ResponseEntity.ok(settings.toString());
    }

    public ResponseEntity<String> getDeviceState(User user, DeviceReqeustDto.deviceRequestDto dto) throws Exception {
        PATEntity patEntity = getPatEntityOrThrow(user, dto.roomId());
        String patToken = decryptPatToken(patEntity);
        String endpoint = "/devices/" + dto.deviceId() + "/state";
        return sendRequest(endpoint, HttpMethod.GET, null, patToken);
    }

    public ResponseEntity<String> controlAirPurifierPower(User user, DeviceReqeustDto.deviceRequestDto dto) throws Exception {
        PATEntity patEntity = getPatEntityOrThrow(user, dto.roomId());
        String patToken = decryptPatToken(patEntity);

        ResponseEntity<String> statusResponse = getDeviceState(user, dto);
        if (!statusResponse.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("디바이스 상태 조회 실패");
        }

        DeviceStateResponseDto state = objectMapper.readValue(statusResponse.getBody(), DeviceStateResponseDto.class);
        String currentMode = state.getResponse().getOperation().getAirFanOperationMode();
        String newMode = currentMode.equals("POWER_ON") ? "POWER_OFF" : "POWER_ON";

        Map<String, Object> requestBody = Map.of(
                "operation", Map.of("airFanOperationMode", newMode)
        );

        String endpoint = "/devices/" + dto.deviceId() + "/control";
        return sendRequest(endpoint, HttpMethod.POST, requestBody, patToken);
    }

    private PATEntity getPatEntityOrThrow(User user, Long roomId) {
        PATEntity pat = patRepository.findByRoomId(roomId)
                .orElseThrow(() -> {
                    log.warn("방 ID {}에 대한 PAT가 존재하지 않습니다.", roomId);
                    return new IllegalStateException("PAT를 찾을 수 없습니다.");
                });

        if(validateAccess(user, pat)) {
            log.info("사용자 ID {}가 방 ID {}에 대한 PAT에 접근할 수 있는 권한이 있습니다.", user.getId(), roomId);
        } else {
            log.warn("사용자 ID {}가 방 ID {}에 대한 PAT에 접근할 수 있는 권한이 없습니다.", user.getId(), roomId);
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }
        return pat;
    }

    private Boolean validateAccess(User user, PATEntity patEntity) {
        //  PAT 엔티티로부터 Room ID를 가져와 Room 정보 조회
        Room room = roomRepository.findById(patEntity.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        boolean hasPermission = false;

        // 1. 요청자가 방장인지 확인
        if (room.getOwner().getId().equals(user.getId())) {
            hasPermission = true;
        }

        // 2. 방장이 아니고, 방 전체 참여자 제어 허용 설정이 true인지 확인
        if (!hasPermission && room.isDeviceControlEnabled()) { // Room.isDeviceControlEnabled() 사용
            hasPermission = true;
        }

        // 3. 위 조건들이 아니고, 개별 참여자에게 제어 권한이 있는지 확인
        if (!hasPermission) {
            Optional<RoomParticipant> participantOptional = roomParticipantRepository.findByRoomAndUser(room, user);
            if (participantOptional.isPresent()) {
                RoomParticipant participant = participantOptional.get();
                if (participant.getCanControlPatDevices()) { // RoomParticipant.getCanControlDevices() 사용 (또는 isCanControlDevices)
                    hasPermission = true;
                }
            }
        }

        return hasPermission;

    }

    private String decryptPatToken(PATEntity patEntity) throws Exception {
        return encryptionUtil.decrypt(patEntity.getEncryptedPat());
    }

    private Device toHvacSetting(Long userId, Long roomId, DeviceResponseWrapper.DeviceResponse device) {
        DeviceResponseWrapper.DeviceInfo info = device.getDeviceInfo();
        Device setting = new Device();
        setting.setUserId(userId);
        setting.setRoomId(roomId);
        setting.setDeviceId(device.getDeviceId());
        setting.setDeviceType(info.getDeviceType());
        setting.setModelName(info.getModelName());
        setting.setAlias(info.getAlias());
        return setting;
    }

    private ResponseEntity<String> sendRequest(String endpoint, HttpMethod method, Object body, String patToken) {
        HttpEntity<Object> request = new HttpEntity<>(body, buildHeaders(patToken));
        String url = baseUrl + endpoint;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, method, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("LG ThinQ API 실패: [{}] {}", response.getStatusCode(), response.getBody());
            }
            return response;
        } catch (Exception e) {
            log.error("LG ThinQ API 요청 실패 [{} {}]: {}", method, url, e.getMessage(), e);
            throw new RuntimeException("LG ThinQ API 요청 중 오류 발생", e);
        }
    }

    private HttpHeaders buildHeaders(String patToken) {
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

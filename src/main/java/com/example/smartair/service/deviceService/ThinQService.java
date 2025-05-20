package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceDto;
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

    public List<DeviceDto> getDeviceList(User user, Long roomId) throws Exception {
        PATEntity patEntity = getPatEntityOrThrow(user, roomId);
        String patToken = decryptPatToken(patEntity);

        String responseJson = sendRequest("/devices", HttpMethod.GET, null, patToken).getBody();
        DeviceResponseWrapper wrapper = objectMapper.readValue(responseJson, DeviceResponseWrapper.class);

        List<Device> settings = wrapper.getResponse().stream()
                .map(device -> toHvacSetting(user, roomId, device))
                .collect(Collectors.toList()); // 디바이스 설정을 Device 객체로 변환

        deviceRepository.saveAll(settings);
        log.info("사용자 ID {}의 방 {}에 대해 {}개의 디바이스를 저장했습니다.", user.getId(), roomId, settings.size());

        return settings.stream()
                .map(device -> {
                    String id = device.getDeviceSerialNumber();
                    String alias = device.getAlias();
                    log.info("디바이스 ID: {}, 디바이스 이름: {}", id, alias);
                    return new DeviceDto(id, alias);
                })
                .collect(Collectors.toList()); // 디바이스 ID와 이름을 포함한 DeviceDto 리스트 반환
    }

    private Device toHvacSetting(User user, Long roomId, DeviceResponseWrapper.DeviceResponse device) {
        DeviceResponseWrapper.DeviceInfo info = device.getDeviceInfo();
        Room room = roomRepository.findRoomById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "방을 찾을 수 없습니다."));

        return Device.builder()
                .user(user)
                .room(room)
                .deviceSerialNumber(device.getDeviceId())
                .deviceType(info.getDeviceType())
                .modelName(info.getModelName())
                .alias(info.getAlias())
                .build();
    }

    public ResponseEntity<String> getDeviceState(User user, Long deviceId) throws Exception {
        Device device = deviceRepository.findById(deviceId).orElseThrow(
                () -> new CustomException(ErrorCode.DEVICE_NOT_FOUND, "디바이스를 찾을 수 없습니다.")
        );

        PATEntity patEntity = getPatEntityOrThrow(user, device.getRoom().getId());
        String patToken = decryptPatToken(patEntity);

        String endpoint = "/devices/" + device.getDeviceSerialNumber()+ "/state";
        return sendRequest(endpoint, HttpMethod.GET, null, patToken);
    }

    public ResponseEntity<String> controlAirPurifierPower(User user, Long deviceId) throws Exception {
        Device device = deviceRepository.findById(deviceId).orElseThrow(
                () -> new CustomException(ErrorCode.DEVICE_NOT_FOUND, "디바이스를 찾을 수 없습니다.")
        );

        PATEntity patEntity = getPatEntityOrThrow(user, device.getRoom().getId());
        String patToken = decryptPatToken(patEntity);

        ResponseEntity<String> statusResponse = getDeviceState(user, deviceId);
        if (!statusResponse.getStatusCode().is2xxSuccessful()) {
            throw new CustomException(ErrorCode.DEVICE_STATE_NOT_FOUND, "디바이스 상태를 가져오는 데 실패했습니다.");
        }

        DeviceStateResponseDto state = objectMapper.readValue(statusResponse.getBody(), DeviceStateResponseDto.class); // 디바이스 상태 DTO로 변환
        String currentMode = state.getResponse().getOperation().getAirFanOperationMode(); // 현재 모드 가져오기
        String newMode = currentMode.equals("POWER_ON") ? "POWER_OFF" : "POWER_ON"; // 전원 상태 반전

        Map<String, Object> requestBody = Map.of(
                "operation", Map.of("airFanOperationMode", newMode)
        );

        String endpoint = "/devices/" + device.getDeviceSerialNumber() + "/control";
        return sendRequest(endpoint, HttpMethod.POST, requestBody, patToken);
    }

    private PATEntity getPatEntityOrThrow(User user, Long roomId) {
        PATEntity pat = patRepository.findByRoomId(roomId)
                .orElseThrow(() -> {
                    log.warn("방 ID {}에 대한 PAT가 존재하지 않습니다.", roomId);
                    return new CustomException(ErrorCode.PAT_NOT_FOUND, "해당 방에 대한 PAT가 존재하지 않습니다.");
                });

        if(validateAccess(user, pat)) {
            log.info("사용자 ID {}가 방 ID {}에 대한 PAT에 접근할 수 있는 권한이 있습니다.", user.getId(), roomId);
        } else {
            log.warn("사용자 ID {}가 방 ID {}에 대한 PAT에 접근할 수 있는 권한이 없습니다.", user.getId(), roomId);
            throw new CustomException(ErrorCode.NO_AUTHORITY, "해당 방에 대한 접근 권한이 없습니다.");
        }
        return pat;
    }

    private Boolean validateAccess(User user, PATEntity patEntity) {
        //  PAT 엔티티로부터 Room ID를 가져와 Room 정보 조회
        Room room = roomRepository.findById(patEntity.getRoomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "Room ID: " + patEntity.getRoomId()));

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

    private ResponseEntity<String> sendRequest(String endpoint, HttpMethod method, Object body, String patToken) { // API 요청 메서드
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
            throw new CustomException(ErrorCode.DEVICE_API_ERROR, "LG ThinQ API 요청 실패" + e.getMessage());
        }
    }

    private HttpHeaders buildHeaders(String patToken) { // 헤더 빌드 메서드
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-message-id", generateMessageId());
        headers.set("x-country", country);
        headers.set("x-api-key", apiKey);
        headers.set("x-client-id", clientId);
        headers.set("Authorization", "Bearer " + patToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String generateMessageId() { // 메시지 ID 생성 메서드
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes())
                .substring(0, 22);
    }
}

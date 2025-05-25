package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceDto;
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


    public List<DeviceDto.DeviceByRoomResponseDto> getDeviceListByRoomId(User user, Long roomId) throws Exception {
        Room room = roomRepository.findRoomById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "방을 찾을 수 없습니다."));

        PATEntity patEntity = getPatEntityOrThrow(user, roomId); // PATEntity 가져오기

        List<DeviceDto.DeviceByRoomResponseDto> result = new ArrayList<>(); // 반환할 디바이스 DTO 리스트

        List<Device> devices = deviceRepository.findByRoomId(roomId); // 방 ID로 디바이스 조회

        for( Device device : devices) {
            String serialNumber = device.getDeviceSerialNumber();
            String modelName = device.getModelName();
            String deviceType = device.getDeviceType();
            String alias = device.getAlias();

            // 디바이스 정보로 DeviceDto 생성
            DeviceDto.DeviceByRoomResponseDto deviceDto = new DeviceDto.DeviceByRoomResponseDto(
                    device.getId(),
                    alias
            );
            result.add(deviceDto);
        }

        return result;
    }
    // 방 ID 를 통해서 디바이스 조회 시, 기존의 device에 없는 디바이스는 새로 생성해서 해당 roomId로 저장, 기존에 있는 device는 roomId 확인해서 동일한것만 반환
    public List<DeviceDto.DeviceAllResponseDto> getDeviceList(User user, Long roomId) throws Exception {

        Room room = roomRepository.findRoomById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "방을 찾을 수 없습니다."));

        PATEntity patEntity = getPatEntityOrThrow(user, roomId); // PATEntity 가져오기
        String patToken = encryptionUtil.decrypt(patEntity.getEncryptedPat());

        String responseJson = sendRequest("/devices", HttpMethod.GET, null, patToken).getBody();
        DeviceResponseWrapper wrapper = objectMapper.readValue(responseJson, DeviceResponseWrapper.class);

        List<DeviceDto.DeviceAllResponseDto> result = new ArrayList<>(); // 반환할 디바이스 DTO 리스트

        for (DeviceResponseWrapper.DeviceResponse deviceRes : wrapper.getResponse()) {
            String serialNumber = deviceRes.getDeviceId();
            DeviceResponseWrapper.DeviceInfo info = deviceRes.getDeviceInfo();

            Optional<Device> existingOpt = deviceRepository.findByDeviceSerialNumber(serialNumber); // 디바이스 시리얼 번호로 기존 디바이스 조회
            Boolean isRegistered = existingOpt.isPresent(); // 기존 디바이스가 있는지 여부

            result.add(
                    new DeviceDto.DeviceAllResponseDto(
                            existingOpt.map(Device::getId).orElse(null), // 기존 디바이스 ID, 없으면 null
                            info.getAlias(), // 디바이스 별칭
                            roomId, // 방 ID
                            isRegistered // 디바이스 등록 여부
                    )
            );
        }

        return result;
    }

    public Object updateDevice(User user, Long roomId, Long deviceId) {
        Room room = roomRepository.findRoomById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "방을 찾을 수 없습니다."));

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND, "디바이스를 찾을 수 없습니다."));

        if(validateAccess(user, room) && validateAccess(user, device.getRoom())) {
            log.info("사용자 ID {}가 방 ID {}에 대한 디바이스에 접근할 수 있는 권한이 있습니다.", user.getId(), roomId);
        } else {
            log.warn("사용자 ID {}가 방 ID {}에 대한 디바이스에 접근할 수 있는 권한이 없습니다.", user.getId(), roomId);
            throw new CustomException(ErrorCode.NO_AUTHORITY, "해당 방에 대한 접근 권한이 없습니다.");
        }

        device.setRoom(room);
        deviceRepository.save(device);
        log.info("디바이스 {}가 방 {}에 업데이트되었습니다.", device.getAlias(), room.getId());

        return new DeviceDto.DeviceUpdateResponseDto(device.getId(), device.getAlias(), roomId); // 디바이스 ID, 이름, 등록 여부, 방 ID 반환
    }

    public String getDeviceState(User user, Long deviceId) throws Exception {
        Device device = deviceRepository.findById(deviceId).orElseThrow(
                () -> new CustomException(ErrorCode.DEVICE_NOT_FOUND, "디바이스를 찾을 수 없습니다.")
        );

        PATEntity patEntity = getPatEntityOrThrow(user, device.getRoom().getId());
        String patToken = encryptionUtil.decrypt(patEntity.getEncryptedPat());

        String endpoint = "/devices/" + device.getDeviceSerialNumber()+ "/state";

        return sendRequest(endpoint, HttpMethod.GET, null, patToken).getBody();
    }

    public ResponseEntity<String> controlAirPurifierPower(User user, Long deviceId) throws Exception {
        Device device = deviceRepository.findById(deviceId).orElseThrow(
                () -> new CustomException(ErrorCode.DEVICE_NOT_FOUND, "디바이스를 찾을 수 없습니다.")
        );

        PATEntity patEntity = getPatEntityOrThrow(user, device.getRoom().getId());
        String patToken = encryptionUtil.decrypt(patEntity.getEncryptedPat());

        String statusResponse = getDeviceState(user, deviceId); // 디바이스 상태 가져오기
        if (statusResponse == null) {
            throw new CustomException(ErrorCode.DEVICE_STATE_NOT_FOUND, "디바이스 상태를 가져오는 데 실패했습니다.");
        }

        DeviceStateResponseDto state = objectMapper.readValue(statusResponse, DeviceStateResponseDto.class); // 디바이스 상태 DTO로 변환
        String currentMode = state.getResponse().getOperation().getAirFanOperationMode(); // 현재 모드 가져오기
        String newMode = currentMode.equals("POWER_ON") ? "POWER_OFF" : "POWER_ON"; // 전원 상태 반전

        Map<String, Object> requestBody = Map.of(
                "operation", Map.of("airFanOperationMode", newMode)
        );

        String endpoint = "/devices/" + device.getDeviceSerialNumber() + "/control";
        return sendRequest(endpoint, HttpMethod.POST, requestBody, patToken);
    }

    public Boolean getAuthentication(User user, Long roomId) {
        try {
            getPatEntityOrThrow(user, roomId);
            return true; // 권한이 있는 경우
        } catch (CustomException e) {
            return false; // 권한이 없는 경우
        }
    }

    private PATEntity getPatEntityOrThrow(User user, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "방을 찾을 수 없습니다."));

        PATEntity pat = patRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("PAT가 존재하지 않습니다.", roomId);
                    return new CustomException(ErrorCode.PAT_NOT_FOUND, "PAT가 존재하지 않습니다.");
                });

        if(validateAccess(user,room)) {
            log.info("사용자 ID {}가 방 ID {}에 대한 PAT에 접근할 수 있는 권한이 있습니다.", user.getId(), roomId);
        } else {
            log.warn("사용자 ID {}가 방 ID {}에 대한 PAT에 접근할 수 있는 권한이 없습니다.", user.getId(), roomId);
            throw new CustomException(ErrorCode.NO_AUTHORITY, "해당 방에 대한 접근 권한이 없습니다.");
        }
        return pat;
    }
    private Boolean validateAccess(User user, Room room) {

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

package com.example.smartair.service.roomService;

import com.example.smartair.dto.roomDto.*;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.roomParticipantRepository.RoomParticipantRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import com.example.smartair.entity.roomParticipant.PatPermissionRequestStatus;


import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;

    public RoomService(UserRepository userRepository, RoomRepository roomRepository, RoomParticipantRepository roomParticipantRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.roomParticipantRepository = roomParticipantRepository;
    }

    /**
     * room 생성
     */
    @Transactional
    public RoomDetailResponseDto createRoom(Long userId, CreateRoomRequestDto createRoomRequestDto) {
        User requestingUser = userRepository.findById(userId).orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));
        
        // 권한 검증: ADMIN 또는 MANAGER만 방 생성 가능
        if (!(requestingUser.getRole() == Role.ADMIN || requestingUser.getRole() == Role.MANAGER)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        Room room = Room.builder()
                .name(createRoomRequestDto.getName())
                .owner(requestingUser) // 방 생성자를 owner로 설정
                .password(createRoomRequestDto.getPassword())
                .deviceControlEnabled(createRoomRequestDto.isDeviceControlEnabled())
                .latitude(createRoomRequestDto.getLatitude())
                .longitude(createRoomRequestDto.getLongitude())
                .build();

        Room savedRoom = roomRepository.save(room);

        // 생성자를 방의 MANAGER로 RoomParticipant에 등록
        RoomParticipant ownerParticipant = new RoomParticipant(savedRoom, requestingUser, Role.MANAGER, true);
        roomParticipantRepository.save(ownerParticipant);

        return RoomDetailResponseDto.from(savedRoom);
    }

    /**
     * 방장이 자신의 방 참여자 목록을 조회합니다.
     */
    public List<ParticipantDetailDto> getRoomParticipants(Long ownerUserId, Long roomId) {
        User requestingUser = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 요청자 ID로 사용자 조회
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 검증: 요청자가 해당 방의 소유자(owner)인지 확인
        if (!room.getOwner().getId().equals(ownerUserId)) {
            // ADMIN은 이 API를 사용하지 않고 AdminService의 getRoomDetail을 사용한다고 가정
            throw new CustomException(ErrorCode.NO_AUTHORITY); 
        }

        return room.getParticipants().stream()
                .map(ParticipantDetailDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 방 참여 (기존 방 등록)
     */
    @Transactional
    public RoomDetailResponseDto joinRoom(Long userId, Long roomId, JoinRoomRequestDto joinRoomRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 방 참여는 별도 역할 권한 검증 없음. (모두 가능)
        // 기존 로직 (중복 참여, 방장 참여 시도, 비밀번호 등) 유지
        if (roomParticipantRepository.existsByRoomAndUser(room, user)) {
            throw new CustomException(ErrorCode.ALREADY_PARTICIPATING_IN_ROOM); 
        }
        if (room.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.OWNER_CANNOT_JOIN_OWN_ROOM); 
        }
        if (room.getPassword() != null && !room.getPassword().isEmpty()) {
            if (joinRoomRequestDto.getPassword() == null || !room.getPassword().equals(joinRoomRequestDto.getPassword())) {
                throw new CustomException(ErrorCode.INVALID_ROOM_PASSWORD); 
            }
        }

        boolean initialDeviceControlEnabled = room.isDeviceControlEnabled();
        RoomParticipant newParticipant = RoomParticipant.builder()
                .room(room)
                .roleInRoom(user.getRole())
                .canControlPatDevices(initialDeviceControlEnabled)
                .patPermissionRequestStatus(PatPermissionRequestStatus.NONE)
                .user(user)
                .build();

        roomParticipantRepository.save(newParticipant);

        room.getParticipants().add(newParticipant); 
        roomRepository.save(room);

        Room updatedRoom = roomRepository.findById(roomId)
                                 .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND)); 
        return RoomDetailResponseDto.from(updatedRoom);
    }

    /**
     * 방 참여자 기기 제어 권한 변경
     */
    @Transactional
    public RoomDetailResponseDto updateParticipantDeviceControl(
            Long actingUserId, 
            Long roomId, 
            Long targetParticipantUserId,
            boolean canControlDevices) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 검증: 방의 owner(MANAGER)이거나 시스템 ADMIN인지 확인 (기존 로직 유지)
        boolean isSystemAdmin = actingUser.getRole() == Role.ADMIN;
        boolean isRoomOwner = room.getOwner().getId().equals(actingUserId);
        if (!isSystemAdmin && !isRoomOwner) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_MANAGE_PARTICIPANTS);
        }

        User targetUser = userRepository.findById(targetParticipantUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        RoomParticipant targetParticipant = roomParticipantRepository.findByRoomAndUser(room, targetUser)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM));

        if (room.getOwner().getId().equals(targetParticipantUserId)) {
            throw new CustomException(ErrorCode.CANNOT_CHANGE_OWNER_DEVICE_CONTROL);
        }

        targetParticipant.setCanControlPatDevices(canControlDevices);
        roomParticipantRepository.save(targetParticipant);
        return RoomDetailResponseDto.from(room);
    }

    /**
     * 방에서 참여자 강퇴 (내보내기)
     */
    @Transactional
    public RoomDetailResponseDto removeParticipantFromRoom(Long actingUserId, Long roomId, Long targetParticipantUserId) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 권한 검증: 방의 owner(MANAGER)이거나 시스템 ADMIN인지 확인 (기존 로직 유지)
        boolean isSystemAdmin = actingUser.getRole() == Role.ADMIN;
        boolean isRoomOwner = room.getOwner().getId().equals(actingUserId);
        if (!isSystemAdmin && !isRoomOwner) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_MANAGE_PARTICIPANTS);
        }

        User targetUser = userRepository.findById(targetParticipantUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); 
        RoomParticipant targetParticipant = roomParticipantRepository.findByRoomAndUser(room, targetUser)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM));

        if (room.getOwner().getId().equals(targetParticipantUserId)) {
            throw new CustomException(ErrorCode.CANNOT_REMOVE_OWNER_FROM_ROOM); 
        }
        if (actingUserId.equals(targetParticipantUserId)) {
            throw new CustomException(ErrorCode.CANNOT_REMOVE_SELF_FROM_ROOM); 
        }
        
        roomParticipantRepository.delete(targetParticipant);
        room.getParticipants().remove(targetParticipant);
        roomRepository.save(room); 
        return RoomDetailResponseDto.from(room);
    }

    /**
     * 참여자가 특정 방의 PAT 장치 제어 권한을 요청합니다.
     *
     * @param requestingUserId 권한을 요청하는 사용자의 ID
     * @param roomId           권한을 요청할 방의 ID
     * @return 처리 결과 메시지
     */
    @Transactional
    public String requestPatDeviceControlPermission(Long requestingUserId, Long roomId) {
        User requestingUser = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        // 방장인 경우 요청 불필요
        if (room.getOwner().getId().equals(requestingUserId)) {
            throw new CustomException(ErrorCode.PAT_PERMISSION_REQUEST_ALREADY_EXISTS);
        }

        RoomParticipant participant = roomParticipantRepository.findByRoomAndUser(room, requestingUser)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM));

        // 방 전체 설정으로 이미 제어 가능한 경우
        if (room.isDeviceControlEnabled()) {
            throw new CustomException(ErrorCode.PAT_PERMISSION_REQUEST_ALREADY_EXISTS);
        }

        // 이미 개별적으로 제어 권한이 있는 경우
        if (participant.getCanControlPatDevices()) {
            throw new CustomException(ErrorCode.PAT_PERMISSION_REQUEST_ALREADY_EXISTS);
        }

        // 이미 요청이 보류 중인 경우
        if (participant.getPatPermissionRequestStatus() == PatPermissionRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.REQUEST_ALREADY_PENDING);
        }

        // 권한 요청 상태를 PENDING으로 변경
        participant.setPatPermissionRequestStatus(PatPermissionRequestStatus.PENDING);
        roomParticipantRepository.save(participant);


        String targetToken = room.getOwner().getFcmToken();
        if (targetToken == null || targetToken.isEmpty()) {
            throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
        }

        Message message = Message.builder()
                .setToken(targetToken)
                .putData("type", "PERMISSION_REQUEST")
                .putData("requesterUserId", String.valueOf(requestingUser.getUsername()))
                .putData("message", "사용자로부터 권한 요청이 도착했습니다.")
                .setNotification(Notification.builder()
                        .setTitle("권한 요청")
                        .setBody("권한 요청이 도착했습니다. 수락하시겠습니까?")
                        .build())
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                // 토큰이 유효하지 않은 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                // 재발급된 이전 토큰인 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else { // 그 외, 오류는 런타임 예외로 처리
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 방장이 특정 참여자의 PAT 장치 제어 권한 요청을 승인합니다.
     *
     * @param actingUserId          요청을 처리하는 방장의 ID
     * @param targetRoomParticipantId 권한을 부여할 RoomParticipant의 ID
     * @return 처리 결과 메시지
     */
    @Transactional
    public String approvePatDeviceControlPermission(Long actingUserId, Long targetRoomParticipantId) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 방장 사용자 조회

        RoomParticipant targetParticipant = roomParticipantRepository.findById(targetRoomParticipantId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM)); 

        Room room = targetParticipant.getRoom();

        // 권한 검증: 요청 처리자가 해당 방의 방장인지 확인
        if (!room.getOwner().getId().equals(actingUserId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY); 
        }

        // 요청 상태 검증: PENDING 상태인 경우에만 처리 가능
        if (targetParticipant.getPatPermissionRequestStatus() != PatPermissionRequestStatus.PENDING) {
            // REQUEST_NOT_PENDING 와 같은 구체적 에러코드 사용 가능
            throw new CustomException(ErrorCode.INVALID_REQUEST_STATUS); 
        }

        // 권한 부여 및 상태 변경
        targetParticipant.setCanControlPatDevices(true);
        targetParticipant.setPatPermissionRequestStatus(PatPermissionRequestStatus.APPROVED);

        roomParticipantRepository.save(targetParticipant);

        String targetToken = targetParticipant.getUser().getFcmToken();

        Message message = Message.builder()
                .setToken(targetToken)
                .putData("type", "PERMISSION_APPROVED")
                .putData("message", "사용자 ID " + targetParticipant.getUser().getUsername() + "의 장치 제어 권한 요청이 승인되었습니다.")
                .setNotification(Notification.builder()
                        .setTitle("권한 승인")
                        .setBody("제어 권한 요청이 승인되었습니다.")
                        .build())
                .build();

        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                // 토큰이 유효하지 않은 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                // 재발급된 이전 토큰인 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else { // 그 외, 오류는 런타임 예외로 처리
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 방장이 특정 참여자의 PAT 장치 제어 권한 요청을 거절합니다.
     *
     * @param actingUserId          요청을 처리하는 방장의 ID
     * @param targetRoomParticipantId 거절할 RoomParticipant의 ID
     * @return 처리 결과 메시지
     */
    @Transactional
    public String rejectPatDeviceControlPermission(Long actingUserId, Long targetRoomParticipantId) {
        User actingUser = userRepository.findById(actingUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND)); // 방장 사용자 조회

        RoomParticipant targetParticipant = roomParticipantRepository.findById(targetRoomParticipantId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM)); // 대상 RoomParticipant 조회 (PARTICIPANT_NOT_FOUND 사용 또는 INVALID_TARGET_PARTICIPANT 추가)

        Room room = targetParticipant.getRoom();

        // 권한 검증: 요청 처리자가 해당 방의 방장인지 확인
        if (!room.getOwner().getId().equals(actingUserId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY); // NO_AUTHORITY_TO_APPROVE_REQUEST 와 같은 구체적 에러코드 사용 가능
        }

        // 요청 상태 검증: PENDING 상태인 경우에만 처리 가능
        if (targetParticipant.getPatPermissionRequestStatus() != PatPermissionRequestStatus.PENDING) {
            // REQUEST_NOT_PENDING 와 같은 구체적 에러코드 사용 가능
            throw new CustomException(ErrorCode.INVALID_REQUEST_STATUS); // 예시, 실제 ErrorCode에 맞게 수정 필요 
        }

        // 권한 상태 변경 (canControlPatDevices는 false로 유지되거나 명시적으로 false 설정)
        targetParticipant.setCanControlPatDevices(false);
        targetParticipant.setPatPermissionRequestStatus(PatPermissionRequestStatus.REJECTED);
        roomParticipantRepository.save(targetParticipant);

        String targetToken = targetParticipant.getUser().getFcmToken();
        Message message = Message.builder()
                .setToken(targetToken)
                .putData("type", "PERMISSION_REJECTED")
                .putData("message", "사용자 ID " + targetParticipant.getUser().getUsername() + "의 장치 제어 권한 요청이 거절되었습니다.")
                .setNotification(Notification.builder()
                        .setTitle("권한 거절")
                        .setBody("제어 권한 요청이 거절되었습니다.")
                        .build())
                .build();
        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                // 토큰이 유효하지 않은 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                // 재발급된 이전 토큰인 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else { // 그 외, 오류는 런타임 예외로 처리
                throw new RuntimeException(e);
            }
        }
    }


}

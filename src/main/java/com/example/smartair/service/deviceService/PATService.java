package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.PATRequestDto;
import com.example.smartair.entity.device.PATEntity;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.deviceRepository.PATRepository;
import com.example.smartair.repository.roomParticipantRepository.RoomParticipantRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.util.EncryptionUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PATService {

    private final PATRepository patRepository;
    private final EncryptionUtil encryptionUtil;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;

    public ResponseEntity<String> savePAT(User user, PATRequestDto request) throws Exception {
        // PAT 토큰을 암호화하여 저장
        String encryptedPatToken = encryptionUtil.encrypt(request.getPatToken());

        // PATEntity 생성 및 저장
        PATEntity patEntity = new PATEntity();
        patEntity.setUserId(user.getId());
        patEntity.setRoomId(request.getRoomId());
        patEntity.setEncryptedPat(encryptedPatToken);
        patEntity.setSetting(request.getSetting());

        patRepository.save(patEntity);

        return ResponseEntity.ok("PAT 토큰이 암호화되어 저장되었습니다.");
    }

    public ResponseEntity<String> updatePATSetting(User user) throws Exception {
        //  사용자 ID로 PAT 엔티티 조회
        PATEntity patEntity = patRepository.findByUserId(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAT_NOT_FOUND));

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

        // 4. 권한에 따른 처리
        if (hasPermission) {
            patEntity.setSetting(!patEntity.getSetting());
            patRepository.save(patEntity);
            String message = patEntity.getSetting() ? "PAT 기기가 켜졌습니다." : "PAT 기기가 꺼졌습니다.";
            return ResponseEntity.ok(message);
        } else {
            // 추후 여기에 "권한 요청" 로직 추가 예정
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("PAT 장치 제어 권한이 없습니다. 방장에게 권한을 요청하세요.");
        }
    }
}

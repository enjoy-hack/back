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

        patRepository.save(patEntity);

        return ResponseEntity.ok("PAT 토큰이 암호화되어 저장되었습니다.");
    }

}

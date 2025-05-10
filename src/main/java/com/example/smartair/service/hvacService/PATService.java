package com.example.smartair.service.hvacService;

import com.example.smartair.dto.hvacDto.PATRequestDto;
import com.example.smartair.entity.hvacSetting.PATEntity;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.hvacRepository.PATRepository;
import com.example.smartair.util.EncryptionUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PATService {

    private final PATRepository patRepository;
    private final EncryptionUtil encryptionUtil;

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
        // PAT 토큰 유효성 검사
        Optional<PATEntity> OptionalPatEntity = patRepository.findByUserId(user.getId());
        if(OptionalPatEntity.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("PAT 토큰이 존재하지 않습니다.");
        }

        PATEntity patEntity = OptionalPatEntity.get();
        patEntity.setSetting(!patEntity.getSetting());

        patRepository.save(patEntity);

        return ResponseEntity.ok("PAT 설정이 변경되었습니다.");
    }
}

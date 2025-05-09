package com.example.smartair.controller.hvacController;

import com.example.smartair.dto.hvacDto.PATRequestDto;
import com.example.smartair.entity.hvacSetting.PATEntity;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.repository.hvacRepository.PATRepository;
import com.example.smartair.util.EncryptionUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/pat")
public class PATController {

    private final PATRepository patRepository;

    public PATController(PATRepository patRepository) {
        this.patRepository = patRepository;
    }

    @PostMapping
    public ResponseEntity<?> savePAT(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody PATRequestDto request ) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
            }

            // PAT 토큰 유효성 검사
            if (request.getPatToken() == null || request.getPatToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("PAT 토큰이 유효하지 않습니다.");
            }

            String encryptedToken = EncryptionUtil.encrypt(request.getPatToken());
            patRepository.save(new PATEntity(userDetails.getUser().getId(), encryptedToken));
            return ResponseEntity.ok("PAT를 암호화하여 저장하였습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("암호화 실패");
        }
    }
}
package com.example.smartair.controller.deviceController;

import com.example.smartair.dto.deviceDto.PATRequestDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.service.deviceService.PATService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/pat")
public class PATController implements PATControllerDocs{

    private final PATService patService;

    @PostMapping()
    public ResponseEntity<?> savePAT(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestBody PATRequestDto request ) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
            }

            // PAT 토큰 유효성 검사
            if (request.getPatToken() == null || request.getPatToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("PAT 토큰이 유효하지 않습니다.");
            }

            return patService.savePAT(userDetails.getUser(), request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("암호화 실패");
        }
    }

    @PutMapping("setting")
    public ResponseEntity<?> updatePATSetting(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
            }

            return patService.updatePATSetting(userDetails.getUser());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PAT 설정 변경 실패");
        }
    }

}
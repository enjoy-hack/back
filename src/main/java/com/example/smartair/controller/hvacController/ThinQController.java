package com.example.smartair.controller.hvacController;

import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.hvacRepository.PATRepository;
import com.example.smartair.service.hvacService.ThinQService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/thinq")
public class ThinQController implements ThinQControllerDocs {

    private final ThinQService thinQService;
    private final PATRepository patRepository;

    public ThinQController(ThinQService thinQService, PATRepository patRepository) {
        this.thinQService = thinQService;
        this.patRepository = patRepository;
    }

    /**
     * 사용자 디바이스 목록 조회
     */
    @GetMapping("/devices")
    public ResponseEntity<String> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        return handleRequestWithPat(userDetails, user -> thinQService.getDeviceList(user));
    }

    /**
     * 특정 디바이스의 상태 조회
     */
    @GetMapping("/{deviceId}/status")
    public ResponseEntity<String> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @PathVariable("deviceId") String deviceId) throws Exception {
        return handleRequestWithPat(userDetails, user -> thinQService.getDeviceStatus(user, deviceId));
    }

    /**
     * 공기청정기 전원 제어
     */
    @PostMapping("/{deviceId}/power")
    public ResponseEntity<String> controlPower(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable("deviceId") String deviceId) throws Exception {
        return handleRequestWithPat(userDetails, user -> thinQService.controlAirPurifierPower(user, deviceId));
    }

    /**
     * PAT 존재 여부 확인 및 공통 처리 핸들러
     */
    private ResponseEntity<String> handleRequestWithPat(CustomUserDetails userDetails,
                                                        ThinQRequestHandler handler) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();

        if (!patRepository.existsByUserId(user.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("PAT가 등록되지 않았습니다. 먼저 PAT를 등록해주세요.");
        }

        return handler.handle(user);
    }

    @FunctionalInterface
    private interface ThinQRequestHandler {
        ResponseEntity<String> handle(User user) throws Exception;
    }
}
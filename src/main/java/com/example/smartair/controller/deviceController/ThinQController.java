package com.example.smartair.controller.deviceController;
;
import com.example.smartair.dto.deviceDto.DeviceReqeustDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.deviceService.ThinQService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/thinq")
@AllArgsConstructor
public class ThinQController implements ThinQControllerDocs {

    private final ThinQService thinQService;

    // 디바이스 전체 목록 조회
    @GetMapping("/devices/all/{roomId}")
    public ResponseEntity<?> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable("roomId") Long roomId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();

        return ResponseEntity.ok(thinQService.getDeviceList(user, roomId));
    }

    // 방번호를 통해 디바이스 등록 목록 조회
    @GetMapping("/devices/registed/{roomId}")
    public ResponseEntity<?> getDevicesByRoomId(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @PathVariable("roomId") Long roomId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();

        return ResponseEntity.ok(thinQService.getDeviceListByRoomId(user, roomId));
    }

    // 방 ID를 통해 디바이스 방 업데이트
    @PutMapping("/{deviceId}/{roomId}")
    public ResponseEntity<?> updateDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable("roomId") Long roomId,
                                            @PathVariable("deviceId") Long deviceId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();

        return ResponseEntity.ok(thinQService.updateDevice(user, roomId, deviceId));
    }

    // 특정 디바이스의 상태 조회
    @GetMapping("/status/{deviceId}")
    public ResponseEntity<?> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @PathVariable("deviceId") Long deviceId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();

        return ResponseEntity.ok(thinQService.getDeviceState(user, deviceId));
    }

    /**
     * 공기청정기 전원 제어
     */
    @PostMapping("/power/{deviceId}")
    public ResponseEntity<?> controlPower(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable("deviceId") Long deviceId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();

        return ResponseEntity.ok(thinQService.controlAirPurifierPower(user, deviceId));
    }

    @GetMapping("/authentication/{roomId}")
    public ResponseEntity<?> authenticateThinQ(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable("roomId") Long roomId) throws Exception {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        User user = userDetails.getUser();
        Boolean authentication = thinQService.getAuthentication(user, roomId);

        return ResponseEntity.ok(authentication);
    }
}
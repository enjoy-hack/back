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

    // 방 ID를 통해 디바이스 목록 조회
    @GetMapping("/devices")
    public ResponseEntity<String> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody DeviceReqeustDto.getDeviceListDto getDeviceListDto) throws Exception {
        return handleRequestWithPat(userDetails, user -> thinQService.getDeviceList(user, getDeviceListDto.roomId()));
    }

    // 특정 디바이스의 상태 조회
    @GetMapping("/{deviceId}/status")
    public ResponseEntity<String> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestBody DeviceReqeustDto.deviceRequestDto deviceRequestDto) throws Exception {
        return handleRequestWithPat(userDetails, user -> thinQService.getDeviceState(user, deviceRequestDto));
    }

    /**
     * 공기청정기 전원 제어
     */
    @PostMapping("/{deviceId}/power")
    public ResponseEntity<String> controlPower(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestBody DeviceReqeustDto.deviceRequestDto deviceRequestDto) throws Exception {
        return handleRequestWithPat(userDetails, user -> thinQService.controlAirPurifierPower(user, deviceRequestDto));
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

        return handler.handle(user);
    }

    @FunctionalInterface
    private interface ThinQRequestHandler {
        ResponseEntity<String> handle(User user) throws Exception;
    }
}
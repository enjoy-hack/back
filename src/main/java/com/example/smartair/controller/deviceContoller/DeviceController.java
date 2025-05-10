package com.example.smartair.controller.deviceContoller;

import com.example.smartair.dto.deviceDto.DeviceRequestDto;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.deviceService.DeviceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class DeviceController implements DeviceControllerDocs {
    private final DeviceService deviceService;

    @PostMapping("/device")
    public ResponseEntity<?> setDevice(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody DeviceRequestDto.setDeviceDto deviceDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        deviceService.setDevice(user, deviceDto);

        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/device")
    public ResponseEntity<?> deleteDevice(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestBody DeviceRequestDto.deleteDeviceDto deviceDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        deviceService.deleteDevice(user, deviceDto);

        return ResponseEntity.ok("success");
    }

    @GetMapping("/devices")
    public ResponseEntity<String> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody Long roomId){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        List<Device> deviceList = deviceService.getDevices(roomId);

        return ResponseEntity.ok(deviceList.toString());
    }

    @GetMapping("/device/status")
    public ResponseEntity<?> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody Long deviceSerialNumber) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        Boolean status = deviceService.getDeviceStatus(deviceSerialNumber);

        return ResponseEntity.ok("device"+ deviceSerialNumber + " running state: " + status);
    }

}


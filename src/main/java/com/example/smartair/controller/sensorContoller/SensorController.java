package com.example.smartair.controller.sensorContoller;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.Sensor.Device;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.sensorService.SensorService;
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
public class SensorController implements SensorControllerDocs {
    private final SensorService sensorService;

    @PostMapping("/device")
    public ResponseEntity<?> setDevice(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody SensorRequestDto.setDeviceDto deviceDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        sensorService.setDevice(user, deviceDto);

        return ResponseEntity.ok("success");
    }

    @DeleteMapping("/device")
    public ResponseEntity<?> deleteDevice(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestBody SensorRequestDto.deleteDeviceDto deviceDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        sensorService.deleteDevice(user, deviceDto);

        return ResponseEntity.ok("success");
    }

    @GetMapping("/devices")
    public ResponseEntity<String> getDevices(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody Long roomId){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        List<Device> deviceList = sensorService.getDevices(roomId);

        return ResponseEntity.ok(deviceList.toString());
    }

    @GetMapping("/device/status")
    public ResponseEntity<?> getDeviceStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody Long deviceSerialNumber) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        Boolean status = sensorService.getDeviceStatus(deviceSerialNumber);

        return ResponseEntity.ok("device"+ deviceSerialNumber + " running state: " + status);
    }

}


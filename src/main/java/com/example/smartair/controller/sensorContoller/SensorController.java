package com.example.smartair.controller.sensorContoller;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.sensor.Sensor;
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
@RequestMapping
public class SensorController implements SensorControllerDocs {
    private final SensorService sensorService;

    @Override
    @PostMapping("/sensor")
    public ResponseEntity<?> setSensor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody SensorRequestDto.setSensorDto sensorDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        sensorService.setSensor(user, sensorDto);

        return ResponseEntity.ok("success");
    }

    @Override
    @DeleteMapping("/sensor")
    public ResponseEntity<?> deleteSensor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestBody SensorRequestDto.deleteSensorDto deviceDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        sensorService.deleteSensor(user, deviceDto);

        return ResponseEntity.ok("success");
    }

    @Override
    @GetMapping("/sensors")
    public ResponseEntity<String> getSensors(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody Long roomId){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        List<Sensor> sensorList = sensorService.getSensors(roomId);

        return ResponseEntity.ok(sensorList.toString());
    }

    @Override
    @GetMapping("/sensor/status")
    public ResponseEntity<?> getSensorStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestBody Long deviceSerialNumber) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        Boolean status = sensorService.getSensorStatus(deviceSerialNumber);

        return ResponseEntity.ok("device"+ deviceSerialNumber + " running state: " + status);
    }

}


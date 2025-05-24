package com.example.smartair.controller.sensorContoller;

import com.example.smartair.dto.roomSensorDto.RoomSensorResponseDto;
import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.dto.sensorDto.SensorResponseDto;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
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
    public ResponseEntity<SensorResponseDto> setSensor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestBody SensorRequestDto.setSensorDto sensorDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        Sensor sensor = sensorService.setSensor(user, sensorDto);

        return ResponseEntity.ok(SensorResponseDto.from(sensor));
    }

    @Override
    @PostMapping("/sensor/room")
    public ResponseEntity<RoomSensorResponseDto> addSensorToRoom(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                 @RequestBody SensorRequestDto.addSensorToRoomDto sensorDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        RoomSensor roomSensor = sensorService.addSensorToRoom(user, sensorDto);

        return ResponseEntity.ok(RoomSensorResponseDto.from(roomSensor));
    }

    @Override
    @DeleteMapping("/sensor")
    public ResponseEntity<?> deleteSensor(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @RequestBody SensorRequestDto.deleteSensorDto deviceDto) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        sensorService.deleteSensor(user, deviceDto);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/sensor/status")
    public ResponseEntity<?> getSensorStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                             @RequestParam String deviceSerialNumber) throws Exception {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Token");
        }
        User user = userDetails.getUser();

        Boolean status = sensorService.getSensorStatus(deviceSerialNumber);

        return ResponseEntity.ok("device"+ deviceSerialNumber + " running state: " + status);
    }

    @Override
    @DeleteMapping("/sensor/room")
    public ResponseEntity<?> unregisterSensorFromRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SensorRequestDto.unregisterSensorFromRoomDto request) throws Exception {
        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        sensorService.unregisterSensorFromRoom(user, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/sensor/{sensorId}")
    public ResponseEntity<SensorResponseDto> getSensorById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long sensorId) {

        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        SensorResponseDto sensorDto = sensorService.getSensorById(sensorId);

        // 센서 소유자 검증 (필요한 경우)
        if (!sensorDto.getOwnerUsername().equals(user.getUsername())) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR, "해당 센서에 대한 권한이 없습니다.");
        }

        return ResponseEntity.ok(sensorDto);
    }

    @Override
    @GetMapping("/user/sensors")
    public ResponseEntity<List<SensorResponseDto>> getUserSensors(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        List<SensorResponseDto> sensors = sensorService.getUserSensors(user);

        return ResponseEntity.ok(sensors);
    }

}


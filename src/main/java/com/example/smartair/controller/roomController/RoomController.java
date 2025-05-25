package com.example.smartair.controller.roomController;

import com.example.smartair.dto.deviceDto.DeviceDto;
import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.JoinRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponseDto;
import com.example.smartair.dto.roomDto.ParticipantDetailDto;
import com.example.smartair.dto.sensorDto.SensorResponseDto;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.user.User;
import com.example.smartair.service.roomService.RoomService;
import com.example.smartair.service.sensorService.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/room")
@Slf4j
public class RoomController implements RoomControllerDocs {

    private final RoomService roomService;

    @Override
    @PostMapping
    public ResponseEntity<RoomDetailResponseDto> createRoom(
            @RequestBody CreateRoomRequestDto createRoomRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUser().getId();
        RoomDetailResponseDto roomDetail = roomService.createRoom(userId, createRoomRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomDetail);
    }

    @Override
    @GetMapping("/{roomId}/participants")
    public ResponseEntity<List<ParticipantDetailDto>> getRoomParticipants(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long ownerUserId = userDetails.getUser().getId();
        List<ParticipantDetailDto> participants = roomService.getRoomParticipants(ownerUserId, roomId);
        return ResponseEntity.ok(participants);
    }

    @Override
    @PostMapping("/{roomId}/join")
    public ResponseEntity<RoomDetailResponseDto> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody JoinRoomRequestDto joinRoomRequestDto) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUser().getId();
        RoomDetailResponseDto roomDetail = roomService.joinRoom(userId, roomId, joinRoomRequestDto);
        return ResponseEntity.ok(roomDetail);
    }

    @Override
    @PatchMapping("/{roomId}/participants/{participantUserId}/device-control")
    public ResponseEntity<RoomDetailResponseDto> updateParticipantDeviceControl(
            @PathVariable Long roomId,
            @PathVariable Long participantUserId,
            @RequestParam boolean allowDeviceControl,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUser().getId();
        RoomDetailResponseDto roomDetail = roomService.updateParticipantDeviceControl(userId, roomId, participantUserId, allowDeviceControl);
        return ResponseEntity.ok(roomDetail);
    }

    @Override
    @DeleteMapping("/{roomId}/participants/{participantUserId}")
    public ResponseEntity<RoomDetailResponseDto> removeParticipantFromRoom(
            @PathVariable Long roomId,
            @PathVariable Long participantUserId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = userDetails.getUser().getId();
        RoomDetailResponseDto roomDetail = roomService.removeParticipantFromRoom(userId, roomId, participantUserId);
        return ResponseEntity.ok(roomDetail);
    }

    // PAT 장치 제어 권한 요청 API
    @Override
    @PostMapping("/{roomId}/pat-permission-request")
    public ResponseEntity<String> requestPatPermission(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long requestingUserId = userDetails.getUser().getId();
        String message = roomService.requestPatDeviceControlPermission(requestingUserId, roomId);
        log.info("PAT 장치 제어 권한 요청: {}", message);
        return ResponseEntity.ok( message);
    }

    // PAT 장치 제어 권한 승인 API
    @Override
    @PostMapping("/pat-permission-request/{roomParticipantId}/approve")
    public ResponseEntity<String> approvePatPermission(
            @PathVariable Long roomParticipantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long actingUserId = userDetails.getUser().getId();
        String message = roomService.approvePatDeviceControlPermission(actingUserId, roomParticipantId);
        log.info("PAT 장치 제어 권한 승인: {}", message);
        return ResponseEntity.ok(message);
    }

    // PAT 장치 제어 권한 거절 API
    @Override
    @PostMapping("/pat-permission-request/{roomParticipantId}/reject")
    public ResponseEntity<String> rejectPatPermission(
            @PathVariable Long roomParticipantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        Long actingUserId = userDetails.getUser().getId();
        String message = roomService.rejectPatDeviceControlPermission(actingUserId, roomParticipantId);
        log.info("PAT 장치 제어 권한 거절: {}", message);
        return ResponseEntity.ok(message);
    }

    @Override
    @GetMapping("/rooms")
    public ResponseEntity<List<RoomDetailResponseDto>> getUserRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        List<RoomDetailResponseDto> rooms = roomService.getUserRooms(user.getId());
        return ResponseEntity.ok(rooms);
    }

//    @Override
//    @GetMapping("/{roomId}/devices")
//    public ResponseEntity<List<DeviceDto>> getRoomDevices(
//            @PathVariable Long roomId,
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//        if (userDetails == null) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//        Long userId = userDetails.getUser().getId();
//        List<DeviceDto> devices = roomService.getRoomDevices(userId, roomId);
//        return ResponseEntity.ok(devices);
//    }

    @Override
    @GetMapping("/{roomId}/sensors")
    public ResponseEntity<List<SensorResponseDto>> getRoomSensors(@PathVariable Long roomId,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userDetails.getUser();

        List<SensorResponseDto> dtos = roomService.getRoomSensors(roomId, user);

        return ResponseEntity.ok(dtos);
    }
} 
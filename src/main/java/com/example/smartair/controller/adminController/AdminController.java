package com.example.smartair.controller.adminController;

import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponseDto;
import com.example.smartair.service.adminService.AdminDeviceService;
import com.example.smartair.service.adminService.AdminRoomService;
import com.example.smartair.service.adminService.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.smartair.dto.userDto.UserDetailResponseDto;
import com.example.smartair.dto.deviceDto.DeviceDetailDto;
import com.example.smartair.dto.sensorDto.SensorDetailDto;
// import org.springframework.security.access.prepost.PreAuthorize; // 필요시 주석 해제

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
// @PreAuthorize("hasRole('ADMIN')") // 클래스 레벨에서 관리자 권한 강제 시
public class AdminController implements AdminControllerDocs {

    private final AdminRoomService adminRoomService;
    private final AdminUserService adminUserService;
    private final AdminDeviceService adminDeviceService;

    // 전체 방 현황 조회
    @Override
    @GetMapping("/rooms")
    // @PreAuthorize("hasRole('ADMIN')") // 메소드 레벨에서 관리자 권한 강제 시
    public ResponseEntity<Page<RoomDetailResponseDto>> getAllRooms(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<RoomDetailResponseDto> rooms = adminRoomService.getAllRoomsDetailForAdmin(pageable);
        return ResponseEntity.ok(rooms);
    }

    // 관리자용 특정 방 상세 조회
    @Override
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDetailResponseDto> getRoomDetailForAdmin(@PathVariable Long roomId) {
        RoomDetailResponseDto roomDetail = adminRoomService.getRoomDetailForAdmin(roomId);
        return ResponseEntity.ok(roomDetail);
    }

    // 관리자용 방 정보 수정
    @Override
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDetailResponseDto> updateRoomForAdmin(
            @PathVariable Long roomId,
            @RequestBody CreateRoomRequestDto requestDto) {
        RoomDetailResponseDto updatedRoom = adminRoomService.updateRoomForAdmin(roomId, requestDto);
        return ResponseEntity.ok(updatedRoom);
    }

    // 관리자용 방 삭제
    @Override
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoomForAdmin(@PathVariable Long roomId) {
        adminRoomService.deleteRoomForAdmin(roomId);
        return ResponseEntity.noContent().build(); // 또는 ResponseEntity.ok().build();
    }

    // 전체 유저 현황 조회
    @Override
    @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')") // 메소드 레벨에서 관리자 권한 강제 시
    public ResponseEntity<Page<UserDetailResponseDto>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserDetailResponseDto> users = adminUserService.getAllUsersDetailForAdmin(pageable);
        return ResponseEntity.ok(users);
    }

    // 전체 기기 현황 조회
    @Override
    @GetMapping("/devices")
    // @PreAuthorize("hasRole('ADMIN')") // 메소드 레벨에서 관리자 권한 강제 시
    public ResponseEntity<Page<DeviceDetailDto>> getAllDevices(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<DeviceDetailDto> devices = adminDeviceService.getAllDevicesDetailForAdmin(pageable);
        return ResponseEntity.ok(devices);
    }

    // 전체 센서 현황 조회
    @Override
    @GetMapping("/sensors")
    // @PreAuthorize("hasRole('ADMIN')") // 메소드 레벨에서 관리자 권한 강제 시
    public ResponseEntity<Page<SensorDetailDto>> getAllSensors(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SensorDetailDto> sensors = adminDeviceService.getAllSensorsDetailForAdmin(pageable);
        return ResponseEntity.ok(sensors);
    }
} 
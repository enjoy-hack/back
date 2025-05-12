package com.example.smartair.controller.adminController;

import com.example.smartair.dto.deviceDto.DeviceDetailDto;
import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponseDto;
import com.example.smartair.dto.sensorDto.SensorDetailDto;
import com.example.smartair.dto.userDto.UserDetailResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin API", description = "관리자 기능 관련 API 명세")
public interface AdminControllerDocs {

    @Operation(summary = "전체 방 현황 조회 (관리자)", description = "시스템의 모든 방 목록을 페이징하여 조회합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 방 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))), // 실제론 Page<RoomDetailResponseDto> 형태
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (관리자 아님)")
    })
    ResponseEntity<Page<RoomDetailResponseDto>> getAllRooms(
            @Parameter(description = "페이지네이션 정보 (예: ?page=0&size=10&sort=createdAt,desc)",
                    schema = @Schema(implementation = Pageable.class)) Pageable pageable);

    @Operation(summary = "특정 방 상세 조회 (관리자)", description = "특정 방의 상세 정보를 조회합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 상세 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = RoomDetailResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<RoomDetailResponseDto> getRoomDetailForAdmin(
            @Parameter(name = "roomId", description = "조회할 방의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId);

    @Operation(summary = "방 정보 수정 (관리자)", description = "특정 방의 정보를 수정합니다. 관리자 권한이 필요합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "방 수정 요청 정보", required = true,
                    content = @Content(schema = @Schema(implementation = CreateRoomRequestDto.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = RoomDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<RoomDetailResponseDto> updateRoomForAdmin(
            @Parameter(name = "roomId", description = "수정할 방의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId,
            @RequestBody CreateRoomRequestDto requestDto);

    @Operation(summary = "방 삭제 (관리자)", description = "특정 방을 삭제합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "방 삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<Void> deleteRoomForAdmin(
            @Parameter(name = "roomId", description = "삭제할 방의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId);

    @Operation(summary = "전체 사용자 현황 조회 (관리자)", description = "시스템의 모든 사용자 목록을 페이징하여 조회합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 사용자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))), // 실제론 Page<UserDetailResponseDto> 형태
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<Page<UserDetailResponseDto>> getAllUsers(
            @Parameter(description = "페이지네이션 정보", schema = @Schema(implementation = Pageable.class)) Pageable pageable);

    @Operation(summary = "전체 기기 현황 조회 (관리자)", description = "시스템의 모든 기기 목록을 페이징하여 조회합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 기기 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))), // 실제론 Page<DeviceDetailDto> 형태
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<Page<DeviceDetailDto>> getAllDevices(
            @Parameter(description = "페이지네이션 정보", schema = @Schema(implementation = Pageable.class)) Pageable pageable);

    @Operation(summary = "전체 센서 현황 조회 (관리자)", description = "시스템의 모든 센서 목록을 페이징하여 조회합니다. 관리자 권한이 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 센서 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))), // 실제론 Page<SensorDetailDto> 형태
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    ResponseEntity<Page<SensorDetailDto>> getAllSensors(
            @Parameter(description = "페이지네이션 정보", schema = @Schema(implementation = Pageable.class)) Pageable pageable);
} 
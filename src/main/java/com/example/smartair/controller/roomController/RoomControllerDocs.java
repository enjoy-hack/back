package com.example.smartair.controller.roomController;

import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.JoinRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponseDto;
import com.example.smartair.dto.roomDto.ParticipantDetailDto;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Room API", description = "방 관련 API 명세")
public interface RoomControllerDocs {

    @Operation(summary = "방 생성", description = "새로운 방을 생성합니다. 시스템 관리자(ADMIN) 또는 매니저(MANAGER) 권한의 사용자만 생성 가능하며, 생성한 사용자가 방의 소유자(MANAGER)가 됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "방 생성 요청 정보", required = true,
                    content = @Content(schema = @Schema(implementation = CreateRoomRequestDto.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "방 생성 성공",
                    content = @Content(schema = @Schema(implementation = RoomDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<RoomDetailResponseDto> createRoom(
            @org.springframework.web.bind.annotation.RequestBody CreateRoomRequestDto createRoomRequestDto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "방 참여", description = "비밀번호와 함께 특정 방에 참여합니다. 참여 시 기기 제어 권한은 방장이 설정한 값으로 설정됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "방 참여 요청 정보", required = true,
                    content = @Content(schema = @Schema(implementation = JoinRoomRequestDto.class))))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "방 참여 성공",
                    content = @Content(schema = @Schema(implementation = RoomDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 잘못된 비밀번호)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "이미 참여한 방"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<RoomDetailResponseDto> joinRoom(
            @Parameter(name = "roomId", description = "참여할 방의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @org.springframework.web.bind.annotation.RequestBody JoinRoomRequestDto joinRoomRequestDto);

    @Operation(summary = "방 참여자 기기 제어 권한 변경", description = "해당 방의 소유자(MANAGER) 또는 시스템 관리자(ADMIN)가 특정 참여자의 기기 제어 권한을 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 변경 성공",
                    content = @Content(schema = @Schema(implementation = RoomDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 방장 권한 변경 시도)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청 사용자, 대상 사용자, 방 또는 참여자를 찾을 수 없음")
    })
    ResponseEntity<RoomDetailResponseDto> updateParticipantDeviceControl(
            @Parameter(name = "roomId", description = "방 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId,
            @Parameter(name = "participantUserId", description = "대상 참여자의 사용자 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long participantUserId,
            @Parameter(name = "allowDeviceControl", description = "기기 제어 허용 여부", required = true, in = ParameterIn.QUERY)
            @RequestParam boolean allowDeviceControl,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "방에서 참여자 강퇴", description = "해당 방의 소유자(MANAGER) 또는 시스템 관리자(ADMIN)가 특정 참여자를 방에서 내보냅니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참여자 강퇴 성공 (업데이트된 방 정보 반환)",
                    content = @Content(schema = @Schema(implementation = RoomDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 방장 강퇴 시도, 자신 강퇴 시도)"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "요청 사용자, 대상 사용자, 방 또는 참여자를 찾을 수 없음")
    })
    ResponseEntity<RoomDetailResponseDto> removeParticipantFromRoom(
            @Parameter(name = "roomId", description = "방 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId,
            @Parameter(name = "participantUserId", description = "강퇴할 참여자의 사용자 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long participantUserId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "방 참여자 목록 조회", description = "방장이 자신의 방에 참여한 사용자 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "참여자 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ParticipantDetailDto.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (방장이 아님)"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<List<ParticipantDetailDto>> getRoomParticipants(
            @Parameter(name = "roomId", description = "조회할 방의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "PAT 장치 제어 권한 요청", description = "참여자가 특정 방의 PAT 장치에 대한 제어 권한을 방장에게 요청합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 요청 성공", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 또는 요청 조건 미충족 (예: 이미 권한 있음, 요청 중복 등)"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 방을 찾을 수 없음")
    })
    ResponseEntity<String> requestPatPermission(
            @Parameter(name = "roomId", description = "권한을 요청할 방의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "PAT 장치 제어 권한 승인", description = "방장이 특정 참여자의 PAT 장치 제어 권한 요청을 승인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 승인 성공", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (방장이 아니거나 요청 상태가 PENDING이 아님)"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 RoomParticipant를 찾을 수 없음")
    })
    ResponseEntity<String> approvePatPermission(
            @Parameter(name = "roomParticipantId", description = "승인할 권한 요청에 해당하는 RoomParticipant의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomParticipantId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "PAT 장치 제어 권한 거절", description = "방장이 특정 참여자의 PAT 장치 제어 권한 요청을 거절합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "권한 거절 성공", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (방장이 아니거나 요청 상태가 PENDING이 아님)"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 RoomParticipant를 찾을 수 없음")
    })
    ResponseEntity<String> rejectPatPermission(
            @Parameter(name = "roomParticipantId", description = "거절할 권한 요청에 해당하는 RoomParticipant의 ID", required = true, in = ParameterIn.PATH)
            @PathVariable Long roomParticipantId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
} 
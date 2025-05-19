package com.example.smartair.service.adminService;

import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponseDto;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.userRepository.UserRepository;
import com.example.smartair.repository.roomParticipantRepository.RoomParticipantRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRoomService {

    private final RoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 관리자용: 전체 방 현황 상세 조회 (페이징)
     * @param pageable 페이징 및 정렬 정보
     * @return Page<RoomDetailResponseDto>
     */
    public Page<RoomDetailResponseDto> getAllRoomsDetailForAdmin(Pageable pageable) {
        Page<Room> roomPage = roomRepository.findAll(pageable);
        return roomPage.map(RoomDetailResponseDto::from);
    }

    /**
     * 관리자용: 특정 방 상세 조회
     * @param roomId 조회할 방의 ID
     * @return RoomDetailResponseDto
     */
    @Transactional(readOnly = true)
    public RoomDetailResponseDto getRoomDetailForAdmin(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "Room ID: " + roomId));
        return RoomDetailResponseDto.from(room);
    }

    /**
     * 관리자용: 방 정보 수정
     * @param roomId 수정할 방의 ID
     * @param requestDto 방 수정 요청 정보
     * @return RoomDetailResponseDto
     */
    @Transactional
    public RoomDetailResponseDto updateRoomForAdmin(Long roomId, CreateRoomRequestDto requestDto) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "Room ID: " + roomId));

        if (requestDto.getName() != null && !requestDto.getName().isEmpty()) {
            room.setName(requestDto.getName());
        }

        if (requestDto.getPassword() != null && !requestDto.getPassword().isEmpty()) {
            room.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        } else if (requestDto.getPassword() != null && requestDto.getPassword().isEmpty()) {
            room.setPassword(null);
        }

        if (requestDto.getLatitude() != null) {
            room.setLatitude(requestDto.getLatitude());
        }

        if (requestDto.getLongitude()!=null){
            room.setLongitude(requestDto.getLongitude());
        }

        room.setDeviceControlEnabled(requestDto.isDeviceControlEnabled());

        return RoomDetailResponseDto.from(room);
    }

    /**
     * 관리자용: 방 삭제
     * @param roomId 삭제할 방의 ID
     */
    @Transactional
    public void deleteRoomForAdmin(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "Room ID: " + roomId));

        List<RoomParticipant> participants = roomParticipantRepository.findByRoom(room);
        roomParticipantRepository.deleteAll(participants);

        roomRepository.delete(room);
    }
} 
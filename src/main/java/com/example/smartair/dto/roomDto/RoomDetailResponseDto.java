package com.example.smartair.dto.roomDto;

import com.example.smartair.entity.room.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class RoomDetailResponseDto {
    private Long id; //방 ID
    private String name;
    private String ownerUsername; // 방장 사용자명
    // private Long placeId;         // 장소 ID - 삭제
    // private String placeName;     // 장소 이름 - 삭제
    private boolean passwordProtected; // 비밀번호 설정 여부
    private boolean deviceControlEnabled; // 방 전체 기기 제어 허용 여부
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private int participantsCount; // 참여자 수
    private List<ParticipantDetailDto> participants; // 참여자 목록
    private double latitude; // 위도
    private double longitude; // 경도

    public static RoomDetailResponseDto from (Room room){
        return RoomDetailResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .ownerUsername(room.getOwner() != null ? room.getOwner().getUsername() : null) 
                // .placeId(room.getPlace() != null ? room.getPlace().getId() : null) // Place 관련 로직 삭제
                // .placeName(room.getPlace() != null ? room.getPlace().getName() : null) // Place 관련 로직 삭제
                .passwordProtected(room.getPassword() != null && !room.getPassword().isEmpty())
                .deviceControlEnabled(room.isDeviceControlEnabled())
                .createdAt(room.getCreatedAt())
                .modifiedAt(room.getModifiedAt())
                .latitude(room.getLatitude())
                .longitude(room.getLongitude())
                .participantsCount(room.getParticipants() != null ? room.getParticipants().size() : 0)
                .participants(room.getParticipants() != null ? 
                              room.getParticipants().stream()
                                  .map(ParticipantDetailDto::from)
                                  .collect(Collectors.toList()) : List.of())
                .build();
    }
}

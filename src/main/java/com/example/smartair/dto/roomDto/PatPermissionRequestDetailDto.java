package com.example.smartair.dto.roomDto;

import com.example.smartair.entity.roomParticipant.PatPermissionRequestStatus;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PatPermissionRequestDetailDto {
    private Long roomParticipantId; // RoomParticipant의 ID
    private Long requesterUserId;   // 요청한 사용자의 ID
    private String requesterName;   // 요청한 사용자의 이름
    private Long roomId;
    private String roomName;
    private PatPermissionRequestStatus status; // 현재 요청 상태

    public static PatPermissionRequestDetailDto from(RoomParticipant participant) {
        return PatPermissionRequestDetailDto.builder()
                .roomParticipantId(participant.getId())
                .requesterUserId(participant.getUser().getId())
                .requesterName(participant.getUser().getUsername()) // 또는 getName() 등 사용자 이름 필드
                .roomId(participant.getRoom().getId())
                .roomName(participant.getRoom().getName())
                .status(participant.getPatPermissionRequestStatus())
                .build();
    }
}
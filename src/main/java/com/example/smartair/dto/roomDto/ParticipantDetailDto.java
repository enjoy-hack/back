package com.example.smartair.dto.roomDto;

import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.user.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantDetailDto {
    private Long userId;
    private String username;
    private Role roleInRoom;
    private Boolean canControlDevices;

    public static ParticipantDetailDto from(RoomParticipant participant) {
        return ParticipantDetailDto.builder()
                .userId(participant.getUser().getId())
                .username(participant.getUser().getUsername())
                .roleInRoom(participant.getRoleInRoom())
                .canControlDevices(participant.getCanControlPatDevices())
                .build();
    }
} 
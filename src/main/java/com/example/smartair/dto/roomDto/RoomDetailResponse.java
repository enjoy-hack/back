package com.example.smartair.dto.roomDto;

import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomDetailResponse {
    private Long id;
    private String name;
    private Place place;
    private User user;

    public static RoomDetailResponse from (Room room){
        return RoomDetailResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .place(room.getPlace())
                .user(room.getUser())
                .build();
    }
}

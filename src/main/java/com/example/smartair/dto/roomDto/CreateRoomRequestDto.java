package com.example.smartair.dto.roomDto;

import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class CreateRoomRequestDto {
    private String name;
    private Place place;
}

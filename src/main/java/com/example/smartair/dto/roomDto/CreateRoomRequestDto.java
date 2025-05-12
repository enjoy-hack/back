package com.example.smartair.dto.roomDto;

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
    private String password;
    private boolean deviceControlEnabled;
    private Double latitude;
    private Double longitude;
}

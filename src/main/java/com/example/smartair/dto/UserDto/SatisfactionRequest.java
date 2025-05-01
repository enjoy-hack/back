package com.example.smartair.dto.UserDto;

import lombok.Data;

@Data
public class SatisfactionRequest {
    private Long userId;
    private int satisfactionScore;
}

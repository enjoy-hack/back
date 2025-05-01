package com.example.smartair.dto.userDto;

import lombok.Data;

@Data
public class SatisfactionRequest {
    private Long userId;
    private int satisfactionScore;
}

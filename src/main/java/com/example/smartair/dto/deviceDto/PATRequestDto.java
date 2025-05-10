package com.example.smartair.dto.deviceDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PATRequestDto {
    private String patToken;
    private Long roomId;
    private Boolean setting; // true: 공개키, false: 비공개키
}

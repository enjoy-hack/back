package com.example.smartair.dto.deviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeviceRequestDto {
    private Long serialNumber;
    private String name;
    private Long roomId;
}

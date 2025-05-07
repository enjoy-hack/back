package com.example.smartair.dto.deviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeviceRequestDto {

    public record setDeviceDto(
            Long serialNumber,
            String name,
            Long roomId
    ){}

    public record deleteDeviceDto(
            Long serialNumber,
            Long roomId
    ){}

}

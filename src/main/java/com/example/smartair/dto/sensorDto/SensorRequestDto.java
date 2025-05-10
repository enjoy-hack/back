package com.example.smartair.dto.sensorDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SensorRequestDto {

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

package com.example.smartair.dto.sensorDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SensorRequestDto {

    public record setSensorDto(
            Long serialNumber,
            String name
    ){}

    public record deleteSensorDto(
            Long serialNumber,
            Long roomId
    ){}

    public record addSensorToRoomDto(
            Long serialNumber,
            Long roomId
    ){}

    public record unregisterSensorFromRoomDto(
            Long serialNumber,
            Long roomId
    ){}
}

package com.example.smartair.dto.sensorDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SensorRequestDto {

    public record setSensorDto(
            String serialNumber,
            String name
    ){}

    public record addSensorToRoomDto(
            String serialNumber,
            Long roomId
    ){}

}

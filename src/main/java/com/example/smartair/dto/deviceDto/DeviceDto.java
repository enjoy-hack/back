package com.example.smartair.dto.deviceDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DeviceDto {

    public record DeviceByRoomResponseDto(
            Long deviceId,
            String alias
    ) {}

    public record DeviceUpdateResponseDto(
            Long deviceId,
            String alias,
            Long roomId
    ) {}

    public record DeviceAllResponseDto(
            Long deviceId,
            String alias,
            Long roomId,
            Boolean isRegistered
    ) {}

}

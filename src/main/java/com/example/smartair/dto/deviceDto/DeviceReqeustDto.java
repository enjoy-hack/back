package com.example.smartair.dto.deviceDto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DeviceReqeustDto {

    public record getDeviceListDto(
            Long roomId
    ){}

    public record deviceRequestDto(
            Long deviceId,
            Long roomId
    ){}

}

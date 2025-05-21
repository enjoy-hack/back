package com.example.smartair.dto.deviceDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DeviceDto {
    private Long deviceId;
    private String alias;
    private Long roomId;

    public DeviceDto(Long deviceId, String alias) {
        this.deviceId = deviceId;
        this.alias = alias; // 디바이스 이름
    }

    public DeviceDto(Long deviceId,String alias,  Long roomId) {
        this.deviceId = deviceId;
        this.alias = alias; // 디바이스 이름
        this.roomId = roomId; // 방 ID
    }
}

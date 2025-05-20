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

    public DeviceDto(Long deviceId, String alias) {
        this.deviceId = deviceId;
        this.alias = alias; // 디바이스 이름
    }
}

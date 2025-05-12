package com.example.smartair.dto.deviceDto;

import com.example.smartair.entity.device.Device;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class DeviceDetailDto {
    private Long id;
    private String deviceId;
    private String deviceType;
    private String modelName;
    private String alias;
    private Long roomId;
    private Long registeredUserId;

    private String roomName;
    private String registeredUsername;

    public static DeviceDetailDto from(Device device) {
        return DeviceDetailDto.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .deviceType(device.getDeviceType())
                .modelName(device.getModelName())
                .alias(device.getAlias())
                .roomId(device.getRoomId())
                .registeredUserId(device.getUserId())
                .build();
    }
} 
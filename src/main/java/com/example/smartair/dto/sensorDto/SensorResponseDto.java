package com.example.smartair.dto.sensorDto;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SensorResponseDto {
    private Long id;
    private String serialNumber;
    private String name;
    private String sensorType;
    private LocalDateTime lastUpdatedAt;
    private String ownerUsername;
    private boolean runningStatus;
    private boolean isRegistered;

    public static SensorResponseDto from(Sensor sensor) {
        User owner = sensor.getUser();

        return SensorResponseDto.builder()
                .id(sensor.getId())
                .serialNumber(sensor.getSerialNumber())
                .name(sensor.getName())
                .sensorType(sensor.getName())
                .lastUpdatedAt(sensor.getModifiedAt())
                .ownerUsername(owner != null ? owner.getUsername() : null)
                .runningStatus(sensor.isRunningStatus())
                .isRegistered(sensor.isRegistered())
                .build();
    }
}

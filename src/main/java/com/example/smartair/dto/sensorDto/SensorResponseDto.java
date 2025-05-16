package com.example.smartair.dto.sensorDto;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SensorResponseDto {
    private String name;
    private String sensorType;
    private LocalDateTime lastUpdatedAt;
    private String ownerUsername;
    private boolean runningStatus;

    public static SensorResponseDto from(Sensor sensor) {
        User owner = sensor.getUser();

        return SensorResponseDto.builder()
                .name(sensor.getName())
                .sensorType(sensor.getName())
                .lastUpdatedAt(sensor.getModifiedAt())
                .ownerUsername(owner != null ? owner.getUsername() : null)
                .runningStatus(sensor.isRunningStatus())
                .build();
    }
}

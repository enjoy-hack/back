package com.example.smartair.dto.sensorDto;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SensorDetailDto {
    private Long id;
    private String name; 
    private String sensorType; 
    private LocalDateTime lastUpdatedAt; 
    private Long ownerUserId; 
    private String ownerUsername; 
    private boolean runningStatus; 
    private Long serialNumber; 

    public static SensorDetailDto from(Sensor sensor) {
        User owner = sensor.getUser();

        return SensorDetailDto.builder()
                .id(sensor.getId())
                .name(sensor.getName()) 
                .sensorType(sensor.getName()) 
                .lastUpdatedAt(sensor.getModifiedAt()) 
                .ownerUserId(owner != null ? owner.getId() : null)
                .ownerUsername(owner != null ? owner.getUsername() : null) 
                .runningStatus(sensor.isRunningStatus()) 
                .serialNumber(sensor.getSerialNumber())
                .build();
    }
} 
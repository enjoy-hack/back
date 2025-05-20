package com.example.smartair.dto.roomSensorDto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SensorRoomMappingDto { //인공지능쪽 보낼 dto
    private String sensorSerialNumber;
    private LocalDateTime sensorRegisterDate;
}
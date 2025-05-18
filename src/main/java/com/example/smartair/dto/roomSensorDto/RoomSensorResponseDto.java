package com.example.smartair.dto.roomSensorDto;

import com.example.smartair.dto.sensorDto.SensorResponseDto;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class RoomSensorResponseDto {
    private Long roomId;
    private Long sensorId;

    public static RoomSensorResponseDto from(RoomSensor roomSensor) {
        return RoomSensorResponseDto.builder()
                .roomId(roomSensor.getRoom().getId())
                .sensorId(roomSensor.getSensor().getId())
                .build();
    }
}

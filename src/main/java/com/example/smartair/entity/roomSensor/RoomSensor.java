package com.example.smartair.entity.roomSensor;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Table(name = "room_sensor")
public class RoomSensor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;
}

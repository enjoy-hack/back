package com.example.smartair.entity.roomDevice;

import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class RoomDevice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;
}

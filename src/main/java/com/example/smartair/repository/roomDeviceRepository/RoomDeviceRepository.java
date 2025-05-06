package com.example.smartair.repository.roomDeviceRepository;

import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomDeviceRepository extends JpaRepository<RoomDevice, Long> {
    Optional<RoomDevice> findByDevice(Device device);

    List<Device> findAllDeviceByRoom(Room room);
}

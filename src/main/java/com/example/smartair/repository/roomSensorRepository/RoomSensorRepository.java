package com.example.smartair.repository.roomSensorRepository;

import com.example.smartair.entity.Sensor.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomSensorRepository extends JpaRepository<RoomDevice, Long> {
    Optional<RoomDevice> findByDevice(Device device);

    Optional<RoomDevice> findByDevice_SerialNumberAndRoom_Id(Long deviceSerialNumber, Long roomId);

    List<RoomDevice> findByRoomId(Long roomId);

    Optional<RoomDevice> findByDevice_SerialNumber(Long serialNumber);

    List<Device> findAllDeviceByRoom(Room room);
}
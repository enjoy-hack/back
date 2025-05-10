package com.example.smartair.repository.roomSensorRepository;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomSensorRepository extends JpaRepository<RoomDevice, Long> {
    Optional<RoomDevice> findBySensor(Sensor sensor);

    Optional<RoomDevice> findBySensor_SerialNumberAndRoom_Id(Long sensorSerialNumber, Long roomId);

    List<RoomDevice> findByRoomId(Long roomId);

    Optional<RoomDevice> findBySensor_SerialNumber(Long serialNumber);

    List<Sensor> findAllDeviceByRoom(Room room);
}
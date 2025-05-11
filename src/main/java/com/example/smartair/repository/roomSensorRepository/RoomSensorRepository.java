package com.example.smartair.repository.roomSensorRepository;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomSensorRepository extends JpaRepository<RoomSensor, Long> {
    Optional<RoomSensor> findBySensor(Sensor sensor);

    Optional<RoomSensor> findBySensor_SerialNumberAndRoom_Id(Long sensorSerialNumber, Long roomId);

    List<RoomSensor> findByRoomId(Long roomId);

    Optional<RoomSensor> findBySensor_SerialNumber(Long serialNumber);

    List<Sensor> findAllDeviceByRoom(Room room);
}
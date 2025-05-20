package com.example.smartair.repository.roomSensorRepository;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomSensorRepository extends JpaRepository<RoomSensor, Long> {
    Optional<RoomSensor> findBySensor(Sensor sensor);

    Optional<RoomSensor> findBySensor_SerialNumberAndRoom_Id(String sensorSerialNumber, Long roomId);

    List<RoomSensor> findByRoomId(Long roomId);

    Optional<RoomSensor> findBySensor_SerialNumber(String serialNumber);

    @Query("SELECT rs.sensor FROM RoomSensor rs WHERE rs.room = :room")
    List<Sensor> findAllSensorByRoom(@Param("room") Room room);

    Optional<RoomSensor> findBySensor_Id(Long sensorId);

    boolean existsBySensorId(Long sensor_id);

    boolean existsBySensor_SerialNumberAndRoom_Id(String serialNumber, Long roomId);
}
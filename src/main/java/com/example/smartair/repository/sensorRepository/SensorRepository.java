package com.example.smartair.repository.sensorRepository;

import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findById(Long id);

    @Query("SELECT das.id FROM Sensor das WHERE das.runningStatus = true")
    List<Long> findAllRunningDeviceIds();

    Optional<Sensor> findBySerialNumber(Long serialNumber);

    List<Sensor> findAllByRunningStatusIsTrue();

    List<Sensor> findByUser(User user);

}

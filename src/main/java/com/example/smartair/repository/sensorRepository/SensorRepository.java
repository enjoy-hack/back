package com.example.smartair.repository.sensorRepository;

import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findBySerialNumber(String serialNumber);

    List<Sensor> findAllByRunningStatusIsTrue();

    List<Sensor> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sensor s WHERE s.serialNumber = :serialNumber")
    Optional<Sensor> findBySerialNumberWithLock(@Param("serialNumber") String serialNumber);

    // 특정 시간 이전에 마지막 데이터를 받은 활성 센서 찾기
    List<Sensor> findByRunningStatusTrueAndLastDataTimeBefore(LocalDateTime threshold);

    // 벌크 업데이트로 오래된 데이터의 센서를 비활성화
    @Modifying
    @Query("UPDATE Sensor s SET s.runningStatus = false WHERE s.lastDataTime < :threshold AND s.runningStatus = true")
    int updateSensorStatusBasedOnLastActivity(@Param("threshold") LocalDateTime threshold);
}

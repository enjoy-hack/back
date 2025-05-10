package com.example.smartair.repository.airQualityRepository.airQualityScoreRepository;

import com.example.smartair.entity.airScore.airQualityScore.DeviceAirQualityScore;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DeviceAirQualityScoreRepository extends JpaRepository<DeviceAirQualityScore, Long> {
    Optional<DeviceAirQualityScore> findFirstByDeviceAirQualityData_SensorOrderByCreatedAtDesc(Sensor sensor);

    @Query("SELECT das FROM DeviceAirQualityScore das JOIN das.deviceAirQualityData aqd " +
           "WHERE aqd.sensor.id = :sensorId " +
           "AND (:startTime IS NULL OR das.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR das.createdAt <= :endTime)")
    Page<DeviceAirQualityScore> findScoresByDeviceAndTimeRange(
            @Param("sensorId") Long sensorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    Optional<DeviceAirQualityScore> findTopByDeviceAirQualityData_SensorOrderByCreatedAtDesc(Sensor sensor);

} 
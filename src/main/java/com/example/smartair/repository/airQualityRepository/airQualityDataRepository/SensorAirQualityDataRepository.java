package com.example.smartair.repository.airQualityRepository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorAirQualityDataRepository extends JpaRepository<SensorAirQualityData, Long> {
    Optional<SensorAirQualityData> findTopBySensorIdOrderByCreatedAtDesc(Long sensorId);

    List <SensorAirQualityData> findAllBySensor_Id(Long sensorId);

    List<SensorAirQualityData> findBySensorAndCreatedAtBetweenOrderByCreatedAtAsc(
            Sensor sensor, LocalDateTime startTime, LocalDateTime endTime);

    List<SensorAirQualityData> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    Optional<SensorAirQualityData> findTopBySensor_SerialNumberOrderByCreatedAtDesc(String serialNumber);

    int deleteByCreatedAtBefore(LocalDateTime createdAt);
}

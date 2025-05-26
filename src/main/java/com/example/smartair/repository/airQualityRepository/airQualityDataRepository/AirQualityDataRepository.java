package com.example.smartair.repository.airQualityRepository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AirQualityDataRepository extends JpaRepository<SensorAirQualityData, Long> {
    List<SensorAirQualityData> findBySensorAndCreatedAtBetweenOrderByCreatedAtAsc(Sensor sensor, LocalDateTime snapshotHour, LocalDateTime nextHour);

    Optional<SensorAirQualityData> findBySensor_Id(Long sensor_id);

    Optional<SensorAirQualityData> findTopBySensor_SerialNumberOrderByCreatedAtDesc(String serialNumber);

    List<SensorAirQualityData> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    int deleteByCreatedAtBefore(LocalDateTime dateTime);

    Optional<SensorAirQualityData> findFirstBySensorAndCreatedAtAfterOrderByCreatedAtDesc(Sensor sensor, LocalDateTime createdAt);

    Optional<SensorAirQualityData> findTopBySensorIdOrderByCreatedAtDesc(Long sensorId);



}

package com.example.smartair.repository.airQualityRepository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AirQualityDataRepository extends JpaRepository<DeviceAirQualityData, Long> {

    List<DeviceAirQualityData> findTop7BySensorIdOrderByCreatedAtDesc(Long sensorId);

    List<DeviceAirQualityData> findBySensorAndCreatedAtBetweenOrderByCreatedAtAsc(Sensor sensor, LocalDateTime snapshotHour, LocalDateTime nextHour);

    Optional<DeviceAirQualityData> findBySensor_Id(Long sensor_id);
}

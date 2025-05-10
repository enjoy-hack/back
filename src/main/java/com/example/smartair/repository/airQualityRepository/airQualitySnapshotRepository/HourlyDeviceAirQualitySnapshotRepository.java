package com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository;

import com.example.smartair.entity.airData.snapshot.HourlyDeviceAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyDeviceAirQualitySnapshotRepository extends JpaRepository<HourlyDeviceAirQualitySnapshot, Long> {
    Optional<HourlyDeviceAirQualitySnapshot> findBySensorAndSnapshotHour(Sensor sensor, LocalDateTime snapshotHour);

    List<HourlyDeviceAirQualitySnapshot> findBySensorAndSnapshotHourBetweenOrderBySnapshotHourAsc(Sensor sensor, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

package com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository;

import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyDeviceAirQualitySnapshotRepository extends JpaRepository<HourlySensorAirQualitySnapshot, Long> {
    Optional<HourlySensorAirQualitySnapshot> findBySensorAndSnapshotHour(Sensor sensor, LocalDateTime snapshotHour);

    List<HourlySensorAirQualitySnapshot> findBySensorAndSnapshotHourBetweenOrderBySnapshotHourAsc(Sensor sensor, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

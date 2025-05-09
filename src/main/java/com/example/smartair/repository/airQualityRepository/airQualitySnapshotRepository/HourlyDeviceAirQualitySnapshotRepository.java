package com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository;

import com.example.smartair.entity.airData.snapshot.HourlyDeviceAirQualitySnapshot;
import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HourlyDeviceAirQualitySnapshotRepository extends JpaRepository<HourlyDeviceAirQualitySnapshot, Long> {
    Optional<HourlyDeviceAirQualitySnapshot> findByDeviceAndSnapshotHour(Device device, LocalDateTime snapshotHour);

    List<HourlyDeviceAirQualitySnapshot> findByDeviceAndSnapshotHourBetweenOrderBySnapshotHourAsc(Device device, LocalDateTime startOfDay, LocalDateTime endOfDay);
}

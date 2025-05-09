package com.example.smartair.repository.airQualityRepository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AirQualityDataRepository extends JpaRepository<DeviceAirQualityData, Long> {

    List<DeviceAirQualityData> findTop7ByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    List<DeviceAirQualityData> findByDeviceAndCreatedAtBetweenOrderByCreatedAtAsc(Device device, LocalDateTime snapshotHour, LocalDateTime nextHour);
}

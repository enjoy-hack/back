package com.example.smartair.repository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirQualityDataRepository extends JpaRepository<DeviceAirQualityData, Long> {

    List<DeviceAirQualityData> findTop7ByDeviceIdOrderByCreatedAtDesc(Long deviceId);
}

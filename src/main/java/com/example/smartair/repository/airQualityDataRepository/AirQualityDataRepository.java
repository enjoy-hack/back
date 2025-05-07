package com.example.smartair.repository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirQualityDataRepository extends JpaRepository<DeviceAirQualityData, Long> {
    Optional<DeviceAirQualityData> findByDevice_Id(Long device_id);
}

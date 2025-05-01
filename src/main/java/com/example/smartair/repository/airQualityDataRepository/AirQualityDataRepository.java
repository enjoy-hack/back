package com.example.smartair.repository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirQualityDataRepository extends JpaRepository<DeviceAirQualityData, Long> {

}

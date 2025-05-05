package com.example.smartair.repository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.RoomAirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomAirQualityDataRepository extends JpaRepository<RoomAirQualityData, Long> {
}

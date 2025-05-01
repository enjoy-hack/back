package com.example.smartair.repository.airQualityDataRepository;

import com.example.smartair.entity.airData.airQualityData.PlaceAirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceAirQualityDataRepository extends JpaRepository<PlaceAirQualityData, Long> {

}

package com.example.smartair.repository;

import com.example.smartair.entity.airData.AirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirQualityDataRepository extends JpaRepository<AirQualityData, Long> {

}

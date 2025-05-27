package com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository;

import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictedAirQualityRepository extends JpaRepository<PredictedAirQualityData, Long> {
    // 예측된 공기질 데이터를 저장하는 메소드
    PredictedAirQualityData save(PredictedAirQualityData predictedAirQualityData);

    // 예측된 공기질 데이터를 조회하는 메소드
    List<PredictedAirQualityData> findByRoomId(Long roomId);

    List<PredictedAirQualityData> findBySensorSerialNumberOrderByTimestamp(String sensorSerialNumber);

    List<PredictedAirQualityData> findAllBySensorSerialNumber(String sensorSerialNumber);
    Optional<PredictedAirQualityData> findBySensorSerialNumberAndTimestamp(String sensorSerialNumber, LocalDateTime timestamp);
}

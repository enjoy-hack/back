package com.example.smartair.repository.airQualityRepository.airQualityScoreRepository;

import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorAirQualityScoreRepository extends JpaRepository<SensorAirQualityScore, Long> {

    @Query("SELECT das FROM SensorAirQualityScore das JOIN das.sensorAirQualityData aqd " +
            "WHERE aqd.sensor.serialNumber = :serialNumber " +
            "AND (:startTime IS NULL OR das.createdAt >= :startTime) " +
            "AND (:endTime IS NULL OR das.createdAt <= :endTime)")
    Page<SensorAirQualityScore> findScoresBySensorAndTimeRange(
            @Param("serialNumber") String serialNumber,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    Optional<SensorAirQualityScore> findTopBySensorAirQualityData_SensorOrderByCreatedAtDesc(Sensor sensor);

    @Query("SELECT das FROM SensorAirQualityScore das JOIN das.sensorAirQualityData aqd " +
            "WHERE aqd.sensor.serialNumber = :serialNumber " +
            "AND (:startTime IS NULL OR das.createdAt >= :startTime) " +
            "AND (:endTime IS NULL OR das.createdAt <= :endTime)")
    List<SensorAirQualityScore> findScoresBySensorSerialNumberAndTimeRange(
            @Param("serialNumber") String serialNumber,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT s FROM SensorAirQualityScore s " +
            "WHERE s.sensorAirQualityData.sensor.serialNumber = :serialNumber " +
            "ORDER BY s.createdAt DESC",
            nativeQuery = false)
    Page<SensorAirQualityScore> findLatestScoresBySerialNumber(@Param("serialNumber") String serialNumber, Pageable pageable);

    default Optional<SensorAirQualityScore> findLatestScoreBySerialNumber(String serialNumber) {
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<SensorAirQualityScore> result = findLatestScoresBySerialNumber(serialNumber, pageRequest);
        return result.hasContent() ? Optional.of(result.getContent().get(0)) : Optional.empty();
    }
}
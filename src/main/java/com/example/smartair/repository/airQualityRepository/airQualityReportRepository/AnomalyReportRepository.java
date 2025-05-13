package com.example.smartair.repository.airQualityRepository.airQualityReportRepository;

import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AnomalyReportRepository extends JpaRepository<AnomalyReport, Long> {

    @Query("SELECT a FROM AnomalyReport a " +
            "WHERE a.sensor = :sensor " +
            "AND a.anomalyTimestamp >= :startDateTime " +
            "AND a.anomalyTimestamp <= :endDateTime " +
            "ORDER BY a.anomalyTimestamp ASC")
    List<AnomalyReport> findAnomaliesBySensorAndDateRange(
            @Param("sensor") Sensor sensor,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );




}

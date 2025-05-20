package com.example.smartair.repository.airQualityRepository.airQualityReportRepository;

import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySensorAirQualityReportRepository extends JpaRepository<DailySensorAirQualityReport, Long> {
    Optional<DailySensorAirQualityReport> findBySensorAndReportDate(Sensor sensor, LocalDate date);
    List<DailySensorAirQualityReport> findBySensorAndReportDateBetweenOrderByReportDateAsc(Sensor sensor, LocalDate startDate, LocalDate endDate);
    List<DailySensorAirQualityReport> findByReportDateBefore(LocalDate date);
    List<DailySensorAirQualityReport> findAllBySensorId(Long sensorId);
    List<DailySensorAirQualityReport> findAllBySensorSerialNumber(String serialNumber);
}

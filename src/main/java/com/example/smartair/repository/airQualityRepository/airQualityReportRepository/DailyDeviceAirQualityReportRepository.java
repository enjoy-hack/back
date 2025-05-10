package com.example.smartair.repository.airQualityRepository.airQualityReportRepository;

import com.example.smartair.entity.airData.report.DailyDeviceAirQualityReport;
import com.example.smartair.entity.sensor.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyDeviceAirQualityReportRepository extends JpaRepository<DailyDeviceAirQualityReport, Long> {
    Optional<DailyDeviceAirQualityReport> findBySensorAndReportDate(Sensor sensor, LocalDate date);
    List<DailyDeviceAirQualityReport> findBySensorAndReportDateBetweenOrderByReportDateAsc(Sensor sensor, LocalDate startDate, LocalDate endDate);
    List<DailyDeviceAirQualityReport> findByReportDateBefore(LocalDate date);
    List<DailyDeviceAirQualityReport> findAllBySensorId(Long sensorId);
}

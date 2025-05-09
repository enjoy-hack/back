package com.example.smartair.repository.airQualityRepository.airQualityReportRepository;

import com.example.smartair.entity.airData.report.DailyDeviceAirQualityReport;
import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyDeviceAirQualityReportRepository extends JpaRepository<DailyDeviceAirQualityReport, Long> {
    Optional<DailyDeviceAirQualityReport> findByDeviceAndReportDate(Device device, LocalDate date);
    List<DailyDeviceAirQualityReport> findByDeviceAndReportDateBetweenOrderByReportDateAsc(Device device, LocalDate startDate, LocalDate endDate);
    List<DailyDeviceAirQualityReport> findByReportDateBefore(LocalDate date);
    List<DailyDeviceAirQualityReport> findAllByDeviceId(Long deviceId);
}

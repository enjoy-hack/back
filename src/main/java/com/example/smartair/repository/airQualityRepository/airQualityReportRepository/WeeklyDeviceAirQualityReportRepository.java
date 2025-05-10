package com.example.smartair.repository.airQualityRepository.airQualityReportRepository;

import com.example.smartair.entity.airData.report.WeeklyDeviceAirQualityReport;
import com.example.smartair.entity.Sensor.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyDeviceAirQualityReportRepository extends JpaRepository<WeeklyDeviceAirQualityReport, Long> {
    Optional<WeeklyDeviceAirQualityReport> findByDeviceAndYearOfWeekAndWeekOfYear(Device device, int year, int weekOfYear);
    List<WeeklyDeviceAirQualityReport> findAllByDeviceId(Long deviceId);
    List<WeeklyDeviceAirQualityReport> findByStartDateOfWeekBefore(LocalDate date);

    // 특정 장치에 대해 주어진 기간과 겹치는 모든 주간 보고서 조회
    // 조건: (리포트의 시작일 <= 기간의 종료일) AND (리포트의 종료일 >= 기간의 시작일)
    @Query("SELECT w FROM WeeklyDeviceAirQualityReport w " +
            "WHERE w.device = :device " +
            "AND w.startDateOfWeek <= :periodEnd " +
            "AND w.endDateOfWeek >= :periodStart " +
            "ORDER BY w.startDateOfWeek ASC")
    List<WeeklyDeviceAirQualityReport> findOverlappingWeeklyReports(
            @Param("device") Device device,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}

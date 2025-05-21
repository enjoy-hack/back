package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.report.WeeklySensorAirQualityReport;
import com.example.smartair.service.airQualityService.report.AnomalyReportService;
import com.example.smartair.service.airQualityService.report.DailyReportService;
import com.example.smartair.service.airQualityService.report.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "AirQuality Report API", description = "일별 및 주간 공기질 리포트 조회 및 관리 API")
public class AirQualityReportController implements AirQualityReportControllerDocs{

    private final DailyReportService dailyReportService;
    private final WeeklyReportService weeklyReportService;
    private final AnomalyReportService anomalyReportService;

    // === 일별 리포트 API ===
    @Override
    @GetMapping("/daily/{serialNumber}/{date}")
    public ResponseEntity<DailySensorAirQualityReport> getDailyReport(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회할 날짜", required = true, example = "2023-10-28")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySensorAirQualityReport report = dailyReportService.getDailyReport(serialNumber, date);
        return ResponseEntity.ok(report);
    }

    @Override
    @GetMapping("/daily/{serialNumber}")
    public ResponseEntity<List<DailySensorAirQualityReport>> getDailyReportsForPeriod(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailySensorAirQualityReport> reports = dailyReportService.getDailyReportsForPeriod(serialNumber, startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    @Override
    @DeleteMapping("/daily/{reportId}/delete")
    public ResponseEntity<Void> deleteDailyReport(
            @Parameter(description = "삭제할 일별 리포트의 ID", required = true, example = "100") @PathVariable Long reportId) {
        dailyReportService.deleteDailyReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/daily/{serialNumber}/deleteAll")
    public ResponseEntity<Integer> deleteDailyReportsByDeviceId(
            @Parameter(description = "모든 일별 리포트를 삭제할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber) {
        int deletedCount = dailyReportService.deleteDailyReportsByDeviceId(serialNumber);
        return ResponseEntity.ok(deletedCount);
    }

    @Override
    @DeleteMapping("/old/daily/{reportId}/delete")
    public ResponseEntity<Integer> deleteOldDailyReports(
            @Parameter(description = "삭제할 일별 리포트의 ID", required = true, example = "100") @RequestParam Integer days) {
        int deletedCount = dailyReportService.deleteOldDailyReports(days);
        return ResponseEntity.ok(deletedCount);
    }

    // === 주간 리포트 API ===
    @Override
    @GetMapping("/weekly/{serialNumber}/{year}/{weekOfYear}")
    public ResponseEntity<WeeklySensorAirQualityReport> getWeeklyReport(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회할 연도 (YYYY 형식)", required = true, example = "2023") @PathVariable Integer year,
            @Parameter(description = "조회할 주차 (1-53 사이의 숫자, ISO 8601 기준)", required = true, example = "43") @PathVariable Integer weekOfYear) {
        WeeklySensorAirQualityReport report = weeklyReportService.getWeeklyReport(serialNumber, year, weekOfYear);
        return ResponseEntity.ok(report);
    }

    @Override
    @GetMapping("/weekly/{serialNumber}")
    public ResponseEntity<List<WeeklySensorAirQualityReport>> getWeeklyReportsForPeriod(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<WeeklySensorAirQualityReport> reports = weeklyReportService.getWeeklyReportsForPeriod(serialNumber, startDate, endDate);
        return ResponseEntity.ok(reports);
    }

    @Override
    @DeleteMapping("/weekly/{reportId}/delete")
    public ResponseEntity<Void> deleteWeeklyReport(
            @Parameter(description = "삭제할 주간 리포트의 ID", required = true, example = "100") @PathVariable Long reportId) {
        weeklyReportService.deleteWeeklyReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/old/weekly/delete")
    public ResponseEntity<Integer> deleteOldWeeklyReports(
            @Parameter(description = "삭제할 주간 리포트의 ID", required = true, example = "100") @RequestParam Integer weeksOld) {
        int deletedCount = weeklyReportService.deleteOldWeeklyReports(weeksOld);
        return ResponseEntity.ok(deletedCount);
    }

    @Override
    @DeleteMapping("/weekly/{serialNumber}/deleteAll")
    public ResponseEntity<Integer> deleteWeeklyReportsByDeviceId(
            @Parameter(description = "모든 주간 리포트를 삭제할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber) {
        int deletedCount = weeklyReportService.deleteWeeklyReportsByDeviceId(serialNumber);
        return ResponseEntity.ok(deletedCount);
    }

    @Override
    @PostMapping("/anomaly")
    public ResponseEntity<?> setAnomalyDailyReport(@RequestBody AnomalyReportDto anomalyReportDto) {
        return ResponseEntity.ok(anomalyReportService.setAnomalyReport(anomalyReportDto));
    }

    @Override
    @GetMapping("/anomaly/{serialNumber}/{startDate}/{endDate}")
    public ResponseEntity<List<AnomalyReport>> getAnomalyReports(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AnomalyReport> anomalyReports = anomalyReportService.getAnomalyReports(serialNumber, startDate, endDate);
        return ResponseEntity.ok(anomalyReports);
    }

} 
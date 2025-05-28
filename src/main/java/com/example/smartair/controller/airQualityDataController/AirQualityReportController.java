package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportResponseDto;
import com.example.smartair.dto.airQualityDataDto.dailyReportDto.DailyReportResponseDto;
import com.example.smartair.dto.airQualityDataDto.weeklyReportDto.WeeklyReportResponseDto;
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
    public ResponseEntity<DailyReportResponseDto> getDailyReport(
            @PathVariable String serialNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySensorAirQualityReport report = dailyReportService.getDailyReport(serialNumber, date);
        return ResponseEntity.ok(DailyReportResponseDto.from(report));
    }

    @Override
    @GetMapping("/daily/{serialNumber}")
    public ResponseEntity<List<DailyReportResponseDto>> getDailyReportsForPeriod(
            @PathVariable String serialNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailySensorAirQualityReport> reports = dailyReportService.getDailyReportsForPeriod(serialNumber, startDate, endDate);
        return ResponseEntity.ok(
                reports.stream()
                        .map(DailyReportResponseDto::from)
                        .toList()
        );
    }

    @Override
    @PostMapping("/daily/{serialNumber}/create")
    public ResponseEntity<DailyReportResponseDto> generateDailyReportManually(
            @PathVariable String serialNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DailySensorAirQualityReport report = dailyReportService.generateDailyReportManually(serialNumber, date);
        return ResponseEntity.ok(DailyReportResponseDto.from(report));
    }

    @Override
    @DeleteMapping("/daily/sensor/{serialNumber}/delete")
    public ResponseEntity<Void> deleteDailyReportBySerialNumber(
            @PathVariable String serialNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        dailyReportService.deleteDailyReportBySerialNumber(serialNumber, date);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/daily/{reportId}/delete")
    public ResponseEntity<Void> deleteDailyReport(
            @PathVariable Long reportId) {
        dailyReportService.deleteDailyReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/daily/{serialNumber}/deleteAll")
    public ResponseEntity<Integer> deleteDailyReportsByDeviceId(
            @PathVariable String serialNumber) {
        int deletedCount = dailyReportService.deleteDailyReportsByDeviceId(serialNumber);
        return ResponseEntity.ok(deletedCount);
    }

    @Override
    @DeleteMapping("/old/daily/delete")
    public ResponseEntity<Integer> deleteOldDailyReports(
            @RequestParam Integer days) {
        int deletedCount = dailyReportService.deleteOldDailyReports(days);
        return ResponseEntity.ok(deletedCount);
    }

    // === 주간 리포트 API ===
    @Override
    @GetMapping("/weekly/{serialNumber}/{year}/{weekOfYear}")
    public ResponseEntity<WeeklyReportResponseDto> getWeeklyReport(
            @PathVariable String serialNumber,
            @PathVariable Integer year,
            @PathVariable Integer weekOfYear) {
        WeeklySensorAirQualityReport report = weeklyReportService.getWeeklyReport(serialNumber, year, weekOfYear);
        return ResponseEntity.ok(WeeklyReportResponseDto.from(report));
    }

    @Override
    @GetMapping("/weekly/{serialNumber}")
    public ResponseEntity<List<WeeklyReportResponseDto>> getWeeklyReportsForPeriod(
            @PathVariable String serialNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<WeeklySensorAirQualityReport> reports = weeklyReportService.getWeeklyReportsForPeriod(serialNumber, startDate, endDate);
        return ResponseEntity.ok(
                reports.stream()
                        .map(WeeklyReportResponseDto::from)
                        .toList()
        );
    }

    @Override
    @PostMapping("/weekly/{serialNumber}/create")
    public ResponseEntity<WeeklyReportResponseDto> generateWeeklyReportManually(
            @PathVariable String serialNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        WeeklySensorAirQualityReport report = weeklyReportService.generateWeeklyReportManually(serialNumber, weekStartDate);
        return ResponseEntity.ok(WeeklyReportResponseDto.from(report));
    }

    @Override
    @DeleteMapping("/weekly/{reportId}/delete")
    public ResponseEntity<Void> deleteWeeklyReport(
            @PathVariable Long reportId) {
        weeklyReportService.deleteWeeklyReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/old/weekly/delete")
    public ResponseEntity<Integer> deleteOldWeeklyReports(
            @RequestParam Integer weeksOld) {
        int deletedCount = weeklyReportService.deleteOldWeeklyReports(weeksOld);
        return ResponseEntity.ok(deletedCount);
    }

    @Override
    @DeleteMapping("/weekly/{serialNumber}/deleteAll")
    public ResponseEntity<Integer> deleteWeeklyReportsByDeviceId(
            @PathVariable String serialNumber) {
        int deletedCount = weeklyReportService.deleteWeeklyReportsByDeviceId(serialNumber);
        return ResponseEntity.ok(deletedCount);
    }

    @Override
    @PostMapping("/anomaly")
    public ResponseEntity<?> setAnomalyDailyReport(@RequestBody AnomalyReportDto anomalyReportDto) throws Exception {
        return ResponseEntity.ok(anomalyReportService.setAnomalyReport(anomalyReportDto));
    }

    @Override
    @GetMapping("/anomaly/{serialNumber}/{startDate}/{endDate}")
    public ResponseEntity<List<AnomalyReportResponseDto>> getAnomalyReports(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<AnomalyReportResponseDto> anomalyReports = anomalyReportService.getAnomalyReports(serialNumber, startDate, endDate);
        return ResponseEntity.ok(anomalyReports);
    }

} 
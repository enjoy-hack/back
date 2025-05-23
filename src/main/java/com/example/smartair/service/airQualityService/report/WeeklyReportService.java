package com.example.smartair.service.airQualityService.report;

import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.report.WeeklySensorAirQualityReport;
import com.example.smartair.entity.airScore.AirQualityGrade;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.WeeklySensorAirQualityReportRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportService {

    private final SensorRepository sensorRepository;
    private final DailyReportService dailyReportService;
    private final WeeklySensorAirQualityReportRepository weeklyReportRepository;

    /**
     * 특정 장치의 특정 연도, 주차에 대한 주간 보고서를 생성하거나 업데이트합니다.
     */
    @Transactional
    public void createOrUpdateWeeklyReport(Long sensorId, int year, int weekOfYear) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "sensor id: " + sensorId));

        Optional<WeeklySensorAirQualityReport> existingReportOpt =
                weeklyReportRepository.findBySensorAndYearOfWeekAndWeekOfYear(sensor, year, weekOfYear);

        // 해당 주의 시작일(월요일)과 종료일(일요일) 계산
        LocalDate firstDayOfWeek = LocalDate.now()
                .with(WeekFields.ISO.weekBasedYear(), year)
                .with(WeekFields.ISO.weekOfWeekBasedYear(), weekOfYear)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastDayOfWeek = firstDayOfWeek.plusDays(6);

        // 해당 주의 일별 보고서 목록을 수집
        List<DailySensorAirQualityReport> dailyReportsForWeek = collectDailyReports(sensorId, firstDayOfWeek, lastDayOfWeek);

        // 일별 보고서가 없는 경우 처리
        if (dailyReportsForWeek.isEmpty()) {
            handleEmptyDailyReports(sensor.getSerialNumber(), year, weekOfYear, existingReportOpt);
        }

        // 기존 보고서가 있으면 업데이트, 없으면 새로 생성
        if (existingReportOpt.isPresent()) {
            updateWeeklyReport(existingReportOpt.get(), dailyReportsForWeek, firstDayOfWeek, lastDayOfWeek);
        } else {
            createNewWeeklyReport(sensor, year, weekOfYear, firstDayOfWeek, lastDayOfWeek, dailyReportsForWeek);
        }
    }

    private List<DailySensorAirQualityReport> collectDailyReports(Long sensorId, LocalDate firstDayOfWeek, LocalDate lastDayOfWeek) {
        List<DailySensorAirQualityReport> dailyReports = new ArrayList<>();

        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "sensor id: " + sensorId));

        for (LocalDate date = firstDayOfWeek; !date.isAfter(lastDayOfWeek); date = date.plusDays(1)) {
            try {
                DailySensorAirQualityReport dailyReport = dailyReportService.getDailyReport(sensor.getSerialNumber(), date);
                dailyReports.add(dailyReport);
            } catch (CustomException e) {
                if (e.getErrorCode() == ErrorCode.REPORT_NOT_FOUND) {
                    log.warn("주간 보고서 생성 중 Sensor SerialNumber: {}의 {} 날짜에 대한 일별 보고서가 없습니다.",
                            sensor.getSerialNumber(), date);
                } else {
                    log.error("주간 보고서 생성 중 예상치 못한 오류 발생: {}", e.getMessage());
                }
            }
        }

        return dailyReports;
    }

    private void handleEmptyDailyReports(String serialNumber, int year, int weekOfYear, Optional<WeeklySensorAirQualityReport> existingReportOpt) {
        String errorMessage = String.format("Sensor SerialNumber: %s의 %d년 %d주차에 유효한 일별 보고서가 없어 주간 보고서를 생성할 수 없습니다.", serialNumber, year, weekOfYear);
        if (existingReportOpt.isEmpty()) {
            log.warn(errorMessage);
            throw new CustomException(ErrorCode.NO_DAILY_REPORTS_FOUND, errorMessage);
        }
        log.warn(errorMessage + " (기존 보고서 ID:{})", existingReportOpt.get().getId());
        throw new CustomException(ErrorCode.NO_DAILY_REPORTS_FOUND, errorMessage);
    }

    private void updateWeeklyReport(WeeklySensorAirQualityReport report, List<DailySensorAirQualityReport> dailyReports,
                                    LocalDate firstDayOfWeek, LocalDate lastDayOfWeek) {
        log.info("Sensor SerialNumber: {}의 {}년 {}주차에 대한 기존 주간 보고서(ID:{})를 업데이트합니다.",
                report.getSensor().getSerialNumber(), report.getYearOfWeek(), report.getWeekOfYear(), report.getId());

        report.setStartDateOfWeek(firstDayOfWeek);
        report.setEndDateOfWeek(lastDayOfWeek);
        updateReportStatistics(report, dailyReports);
        weeklyReportRepository.save(report);
    }

    private void createNewWeeklyReport(Sensor sensor, int year, int weekOfYear,
                                        LocalDate firstDayOfWeek, LocalDate lastDayOfWeek,
                                        List<DailySensorAirQualityReport> dailyReports) {
        log.info("Sensor SerialNumber: {}의 {}년 {}주차에 대한 새 주간 보고서를 생성합니다.", sensor.getSerialNumber(), year, weekOfYear);

        WeeklySensorAirQualityReport newReport = WeeklySensorAirQualityReport.builder()
                .sensor(sensor)
                .yearOfWeek(year)
                .weekOfYear(weekOfYear)
                .startDateOfWeek(firstDayOfWeek)
                .endDateOfWeek(lastDayOfWeek)
                .dailyReports(dailyReports)
                .build();

        calculateAndSetWeeklyStatistics(newReport, dailyReports);
        weeklyReportRepository.save(newReport);
    }

    public void updateReportStatistics (WeeklySensorAirQualityReport report, List<DailySensorAirQualityReport> dailyReports) {
        report.setDailyReports(dailyReports);
        report.setValidDailyReportCount(dailyReports.size());
        report.setTotalDataPointCount(dailyReports.stream()
                .mapToInt(dr -> dr.getValidDataPointCount() != null ? dr.getValidDataPointCount() : 0)
                .sum());

        calculateAndSetWeeklyStatistics(report, dailyReports);

        //공기질 등급 설정
        updateAirQualityGrade(report);
    }

    public void updateAirQualityGrade(WeeklySensorAirQualityReport report) {
        // 공기질 등급 설정
        Double weeklyOverallScore = report.getWeeklyOverallScore();
        if (weeklyOverallScore != null) {
            try {
                report.setAirQualityGrade(AirQualityGrade.fromScore(weeklyOverallScore));
            } catch (IllegalArgumentException e) {
                log.warn("주간 보고서(ID:{})의 전체 점수({})에 대한 유효한 공기질 등급을 찾을 수 없습니다.", report.getId(), weeklyOverallScore);
                report.setAirQualityGrade(null); // 또는 기본 등급 설정
            }
        } else {
            report.setAirQualityGrade(null);
        }
    }

    /**
     * 특정 장치의 특정 연도, 주차에 대한 주간 보고서를 조회합니다.
     */
    @Transactional(readOnly = true)
    public WeeklySensorAirQualityReport getWeeklyReport(String serialNumber, int year, int weekOfYear) {
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "sensor serialNumber: " + serialNumber));
        return weeklyReportRepository.findBySensorAndYearOfWeekAndWeekOfYear(sensor, year, weekOfYear)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND, "report not found with sensor serialNumber: " + serialNumber + ", year: " + year + ", weekOfYear: " + weekOfYear));
    }

    /**
     * 특정 장치의 특정 기간(시작일, 종료일 기준) 동안 포함되는 모든 주간 보고서 목록을 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<WeeklySensorAirQualityReport> getWeeklyReportsForPeriod(String serialNumber, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE, "시작일이 종료일보다 늦을 수 없습니다.");
        }
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "sensor serialNumber: " + serialNumber));

        // 주간 보고서의 시작일이 주어진 기간 내에 있는 모든 주간 보고서 조회
        List<WeeklySensorAirQualityReport> reportsInPeriod =
                weeklyReportRepository.findOverlappingWeeklyReports(sensor, startDate, endDate);

        log.info("Sensor serialNumber: {}의 {}부터 {}까지의 주간 보고서 {}건 조회 완료", serialNumber, startDate, endDate, reportsInPeriod.size());
        return reportsInPeriod;
    }

    /**
     * 특정 ID의 주간 보고서를 삭제합니다.
     * 주간 보고서가 삭제되면 연관된 일별 보고서도 모두 삭제됩니다.
     */
    @Transactional
    public void deleteWeeklyReport(Long reportId) {
        WeeklySensorAirQualityReport report = weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND, "report not found with id: " + reportId));
        weeklyReportRepository.delete(report);

        log.info("주간 보고서 ID: {} 삭제 완료", reportId);
    }

    /**
     * 생성된 지 N주가 지난 오래된 주간 보고서를 삭제합니다. (예: 2주 = 14일 이전의 '주의 시작일' 기준)
     */
    @Transactional
    public int deleteOldWeeklyReports(int weeksOld) {
        if (weeksOld <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_DATA, "weeksOld는 1 이상이어야 합니다.");
        }
        // 주의 시작일을 기준으로 weeksOld 이전의 보고서를 찾음
        LocalDate cutoffDate = LocalDate.now().minusWeeks(weeksOld).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        log.info("{} 이전 (주의 시작일 기준) 주간 보고서 삭제 시작 ({}주 이전 데이터)", cutoffDate, weeksOld);

        List<WeeklySensorAirQualityReport> oldReports = weeklyReportRepository.findByStartDateOfWeekBefore(cutoffDate);
        if (oldReports.isEmpty()) {
            log.info("{} 이전의 삭제할 주간 보고서가 없습니다.", cutoffDate);
            return 0;
        }
        weeklyReportRepository.deleteAllInBatch(oldReports);
        log.info("{} 이전 주간 보고서 {}개 삭제 완료", cutoffDate, oldReports.size());
        return oldReports.size();
    }

    /**
     * 특정 장치와 관련된 모든 주간 보고서를 삭제합니다.
     */
    @Transactional
    public int deleteWeeklyReportsByDeviceId(String serialNumber) {
        if (sensorRepository.findBySerialNumber(serialNumber).isEmpty()) {
            throw new CustomException(ErrorCode.SENSOR_NOT_FOUND, "sensor serialNumber: " + serialNumber);
        }
        log.info("sensor SerialNumber: {} 관련 모든 주간 보고서 삭제 시작", serialNumber);
        List<WeeklySensorAirQualityReport> reportsToDelete = weeklyReportRepository.findAllBySensorSerialNumber(serialNumber);
        if (reportsToDelete.isEmpty()) {
            log.info("sensor SerialNumber: {} 관련 삭제할 주간 보고서가 없습니다.", serialNumber);
            return 0;
        }
        weeklyReportRepository.deleteAllInBatch(reportsToDelete);
        log.info("sensor SerialNumber: {} 관련 주간 보고서 {}개 삭제 완료",serialNumber, reportsToDelete.size());
        return reportsToDelete.size();
    }


    private void calculateAndSetWeeklyStatistics(WeeklySensorAirQualityReport report, List<DailySensorAirQualityReport> dailyReports) {
        if (dailyReports == null || dailyReports.isEmpty()) {
            setDefaultWeeklyStatistics(report); // 일별 보고서가 없으면 기본값 설정
            return;
        }

        // 주간 평균 온도
        DoubleSummaryStatistics tempStats = dailyReports.stream()
                .map(DailySensorAirQualityReport::getDailyAvgTemperature)
                .filter(Objects::nonNull)
                .collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyAvgTemperature(tempStats.getCount() > 0 ? tempStats.getAverage() : null);
        report.setWeeklyMaxTemperature(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMaxTemperature).filter(Objects::nonNull).max(Double::compare).orElse(null));
        report.setWeeklyMinTemperature(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMinTemperature).filter(Objects::nonNull).min(Double::compare).orElse(null));

        // 주간 평균 습도
        DoubleSummaryStatistics humidityStats = dailyReports.stream()
                .map(DailySensorAirQualityReport::getDailyAvgHumidity)
                .filter(Objects::nonNull)
                .collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyAvgHumidity(humidityStats.getCount() > 0 ? humidityStats.getAverage() : null);
        report.setWeeklyMaxHumidity(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMaxHumidity).filter(Objects::nonNull).max(Double::compare).orElse(null));
        report.setWeeklyMinHumidity(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMinHumidity).filter(Objects::nonNull).min(Double::compare).orElse(null));

        // 주간 평균 TVOC
        DoubleSummaryStatistics tvocStats = dailyReports.stream()
                .map(DailySensorAirQualityReport::getDailyAvgTvoc)
                .filter(Objects::nonNull)
                .collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyAvgTvoc(tvocStats.getCount() > 0 ? tvocStats.getAverage() : null);
        report.setWeeklyMaxTvoc(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMaxTvoc).filter(Objects::nonNull).max(Integer::compare).orElse(null));

        // 주간 평균 eCO2
        DoubleSummaryStatistics eco2Stats = dailyReports.stream()
                .map(DailySensorAirQualityReport::getDailyAvgEco2)
                .filter(Objects::nonNull)
                .collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyAvgEco2(eco2Stats.getCount() > 0 ? eco2Stats.getAverage() : null);
        report.setWeeklyMaxEco2(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMaxEco2).filter(Objects::nonNull).max(Integer::compare).orElse(null));

        // 주간 평균 PM2.5
        DoubleSummaryStatistics pm25Stats = dailyReports.stream()
                .map(DailySensorAirQualityReport::getDailyAvgPm25)
                .filter(Objects::nonNull)
                .collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyAvgPm25(pm25Stats.getCount() > 0 ? pm25Stats.getAverage() : null);
        report.setWeeklyMaxPm25(dailyReports.stream().map(DailySensorAirQualityReport::getDailyMaxPm25).filter(Objects::nonNull).max(Double::compare).orElse(null));

        // 주간 평균 점수들
        DoubleSummaryStatistics overallScoreStats = dailyReports.stream().map(DailySensorAirQualityReport::getDailyOverallScore).filter(Objects::nonNull).collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyOverallScore(overallScoreStats.getCount() > 0 ? overallScoreStats.getAverage() : 0.0);
        DoubleSummaryStatistics pm25ScoreStats = dailyReports.stream().map(DailySensorAirQualityReport::getDailyPm25Score).filter(Objects::nonNull).collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyPm25Score(pm25ScoreStats.getCount() > 0 ? pm25ScoreStats.getAverage() : 0.0);
        DoubleSummaryStatistics eco2ScoreStats = dailyReports.stream().map(DailySensorAirQualityReport::getDailyEco2Score).filter(Objects::nonNull).collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyEco2Score(eco2ScoreStats.getCount() > 0 ? eco2ScoreStats.getAverage() : 0.0);
        DoubleSummaryStatistics tvocScoreStats = dailyReports.stream().map(DailySensorAirQualityReport::getDailyTvocScore).filter(Objects::nonNull).collect(Collectors.summarizingDouble(Double::doubleValue));
        report.setWeeklyTvocScore(tvocScoreStats.getCount() > 0 ? tvocScoreStats.getAverage() : 0.0);
        
        // 경향성 계산
        calculateAndSetWeeklyTrends(report, dailyReports);
    }

    private void calculateAndSetWeeklyTrends(WeeklySensorAirQualityReport report, List<DailySensorAirQualityReport> dailyReports) {
        if (dailyReports == null || dailyReports.stream().filter(dr -> dr != null).count() < 2) { // 유효한 일별 보고서가 2개 미만인 경우
            setDefaultTrends(report);
            return;
        }

        List<Double> dailyAvgTemperatures = dailyReports.stream()
                .map(dr -> dr != null ? dr.getDailyAvgTemperature() : null)
                .collect(Collectors.toList());

        List<Double> dailyAvgHumidities = dailyReports.stream()
                .map(dr -> dr != null ? dr.getDailyAvgHumidity() : null)
                .collect(Collectors.toList());

        List<Double> dailyAvgPm25s = dailyReports.stream()
                .map(dr -> dr != null ? dr.getDailyAvgPm25() : null)
                .collect(Collectors.toList());

        List<Double> dailyAvgEco2s = dailyReports.stream()
                .map(dr -> {
                    if (dr == null) return null;
                    Number eco2 = dr.getDailyAvgEco2(); // Can be Double or Integer
                    return (eco2 == null) ? null : eco2.doubleValue();
                })
                .collect(Collectors.toList());

        report.setTemperatureTrend(calculateLinearRegressionSlope(dailyAvgTemperatures));
        report.setHumidityTrend(calculateLinearRegressionSlope(dailyAvgHumidities));
        report.setPm25Trend(calculateLinearRegressionSlope(dailyAvgPm25s));
        report.setEco2Trend(calculateLinearRegressionSlope(dailyAvgEco2s));
    }

    private void setDefaultTrends(WeeklySensorAirQualityReport report) {
        report.setTemperatureTrend(0.0);
        report.setHumidityTrend(0.0);
        report.setPm25Trend(0.0);
        report.setEco2Trend(0.0);
    }

    /**
     * Calculates the slope of the linear regression line using the Least Squares Method.
     * X values are implicitly 0, 1, 2, ... for the provided yValues.
     *
     * @param yValues List of Y-values (e.g., daily average temperatures). Null values are filtered out.
     * @return The slope of the regression line. Returns 0.0 if not enough data points (less than 2).
     */
    private Double calculateLinearRegressionSlope(List<Double> yValues) {
        if (yValues == null) {
            return 0.0;
        }
        // Filter out nulls and ensure we have at least two points
        List<Double> validYValues = yValues.stream().filter(Objects::nonNull).collect(Collectors.toList());
        int n = validYValues.size();

        if (n < 2) {
            return 0.0; // Not enough data points for a meaningful trend
        }

        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumXSquare = 0;

        // X values are 0, 1, 2, ..., n-1
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = validYValues.get(i);

            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXSquare += x * x;
        }

        double denominator = (n * sumXSquare) - (sumX * sumX);

        // Avoid division by zero if denominator is very close to zero
        if (Math.abs(denominator) < 1e-9) {
            return 0.0;
        }

        return ((n * sumXY) - (sumX * sumY)) / denominator;
    }

    private void setDefaultWeeklyStatistics(WeeklySensorAirQualityReport report){
        report.setWeeklyAvgTemperature(null);
        report.setWeeklyAvgHumidity(null);
        report.setWeeklyAvgTvoc(null);
        report.setWeeklyAvgEco2(null);
        report.setWeeklyAvgPm25(null);
        report.setWeeklyOverallScore(0.0);
        report.setWeeklyPm25Score(0.0);
        report.setWeeklyEco2Score(0.0);
        report.setWeeklyTvocScore(0.0);
        report.setWeeklyMaxTemperature(null);
        report.setWeeklyMinTemperature(null);
        report.setWeeklyMaxHumidity(null);
        report.setWeeklyMinHumidity(null);
        report.setWeeklyMaxTvoc(null);
        report.setWeeklyMaxEco2(null);
        report.setWeeklyMaxPm25(null);
        report.setValidDailyReportCount(0);
        report.setTotalDataPointCount(0);
        report.setTemperatureTrend(0.0);
        report.setHumidityTrend(0.0);
        report.setAirQualityGrade(null);
    }
}
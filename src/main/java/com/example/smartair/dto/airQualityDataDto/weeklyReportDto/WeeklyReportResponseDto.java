package com.example.smartair.dto.airQualityDataDto.weeklyReportDto;

import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.report.WeeklySensorAirQualityReport;
import com.example.smartair.entity.airScore.AirQualityGrade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyReportResponseDto {
    private Long id;
    private Long sensorId;
    private String serialNumber;
    private Integer yearOfWeek;
    private Integer weekOfYear;
    private LocalDate startDateOfWeek;
    private LocalDate endDateOfWeek;

    // 주간 평균 데이터
    private Double weeklyAvgTemperature;
    private Double weeklyAvgHumidity;
    private Double weeklyAvgTvoc;
    private Double weeklyAvgEco2;
    private Double weeklyAvgPm25;

    // 주간 평균 점수
    private Double weeklyOverallScore;
    private Double weeklyPm25Score;
    private Double weeklyEco2Score;
    private Double weeklyTvocScore;

    // 주간 최고/최저 값
    private Double weeklyMaxTemperature;
    private Double weeklyMinTemperature;
    private Double weeklyMaxHumidity;
    private Double weeklyMinHumidity;
    private Double weeklyMaxPm25;
    private Integer weeklyMaxTvoc;
    private Integer weeklyMaxEco2;

    // 주간 유효 데이터 통계
    private Integer validDailyReportCount;
    private Integer totalDataPointCount;

    // 주간 경향성 데이터
    private Double temperatureTrend;
    private Double humidityTrend;
    private Double pm25Trend;
    private Double eco2Trend;

    // 주간 품질 등급
    private AirQualityGrade airQualityGrade;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static WeeklyReportResponseDto from(WeeklySensorAirQualityReport report) {
        return WeeklyReportResponseDto.builder()
                .id(report.getId())
                .sensorId(report.getSensor().getId())
                .serialNumber(report.getSensor().getSerialNumber())
                .yearOfWeek(report.getYearOfWeek())
                .weekOfYear(report.getWeekOfYear())
                .startDateOfWeek(report.getStartDateOfWeek())
                .endDateOfWeek(report.getEndDateOfWeek())
                .weeklyAvgTemperature(report.getWeeklyAvgTemperature())
                .weeklyAvgHumidity(report.getWeeklyAvgHumidity())
                .weeklyAvgTvoc(report.getWeeklyAvgTvoc())
                .weeklyAvgEco2(report.getWeeklyAvgEco2())
                .weeklyAvgPm25(report.getWeeklyAvgPm25())
                .weeklyOverallScore(report.getWeeklyOverallScore())
                .weeklyPm25Score(report.getWeeklyPm25Score())
                .weeklyEco2Score(report.getWeeklyEco2Score())
                .weeklyTvocScore(report.getWeeklyTvocScore())
                .weeklyMaxTemperature(report.getWeeklyMaxTemperature())
                .weeklyMinTemperature(report.getWeeklyMinTemperature())
                .weeklyMaxHumidity(report.getWeeklyMaxHumidity())
                .weeklyMinHumidity(report.getWeeklyMinHumidity())
                .weeklyMaxPm25(report.getWeeklyMaxPm25())
                .weeklyMaxTvoc(report.getWeeklyMaxTvoc())
                .weeklyMaxEco2(report.getWeeklyMaxEco2())
                .validDailyReportCount(report.getValidDailyReportCount())
                .totalDataPointCount(report.getTotalDataPointCount())
                .temperatureTrend(report.getTemperatureTrend())
                .humidityTrend(report.getHumidityTrend())
                .pm25Trend(report.getPm25Trend())
                .eco2Trend(report.getEco2Trend())
                .airQualityGrade(report.getAirQualityGrade())
                .build();
    }
}
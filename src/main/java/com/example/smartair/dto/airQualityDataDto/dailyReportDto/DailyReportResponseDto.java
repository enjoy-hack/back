package com.example.smartair.dto.airQualityDataDto.dailyReportDto;

import com.example.smartair.dto.airQualityDataDto.HourlySensorAirQualitySnapshotResponse;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyReportResponseDto {
    private Long id; // 보고서 ID
    private Long sensorId; // 센서 ID
    private String serialNumber; // 센서 일련번호
    private LocalDate reportDate; // 보고서 날짜
    // 일일 평균 데이터
    private Double dailyAvgTemperature;
    private Double dailyAvgHumidity;
    private Double dailyAvgTvoc;
    private Double dailyAvgEco2;
    private Double dailyAvgPm25;

    // 일일 평균 점수
    private Double dailyOverallScore;
    private Double dailyPm25Score;
    private Double dailyEco2Score;
    private Double dailyTvocScore;

    // 최고/최저 값
    private Double dailyMaxTemperature;
    private Double dailyMinTemperature;
    private Double dailyMaxHumidity;
    private Double dailyMinHumidity;
    private Double dailyMaxPm25;
    private Integer dailyMaxTvoc;
    private Integer dailyMaxEco2;

    // 유효 데이터 포인트 수
    private Integer validDataPointCount;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static DailyReportResponseDto from(DailySensorAirQualityReport report) {
        return DailyReportResponseDto.builder()
                .id(report.getId())
                .sensorId(report.getSensor().getId())
                .serialNumber(report.getSensor().getSerialNumber())
                .reportDate(report.getReportDate())
                .dailyAvgTemperature(report.getDailyAvgTemperature())
                .dailyAvgHumidity(report.getDailyAvgHumidity())
                .dailyAvgTvoc(report.getDailyAvgTvoc())
                .dailyAvgEco2(report.getDailyAvgEco2())
                .dailyAvgPm25(report.getDailyAvgPm25())
                .dailyOverallScore(report.getDailyOverallScore())
                .dailyPm25Score(report.getDailyPm25Score())
                .dailyEco2Score(report.getDailyEco2Score())
                .dailyTvocScore(report.getDailyTvocScore())
                .dailyMaxTemperature(report.getDailyMaxTemperature())
                .dailyMinTemperature(report.getDailyMinTemperature())
                .dailyMaxHumidity(report.getDailyMaxHumidity())
                .dailyMinHumidity(report.getDailyMinHumidity())
                .dailyMaxPm25(report.getDailyMaxPm25())
                .dailyMaxTvoc(report.getDailyMaxTvoc())
                .dailyMaxEco2(report.getDailyMaxEco2())
                .validDataPointCount(report.getValidDataPointCount())
                .build();
    }
}

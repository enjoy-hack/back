package com.example.smartair.dto.airQualityDataDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyReportResponseDto {

    private String sensorSerialNumber; // 센서 ID
    private LocalDateTime anomalyTimestamp; // 이상치 발생 시각
    private String pollutant; // 예: "TVOC", "PM10", "CO2"
    private Double pollutantValue; // 이상치 값
    private String description; // 이상치에 대한 설명 또는 분석 결과

    private SnapshotData snapshotData; // 관련 시간별 스냅샷 데이터

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SnapshotData {
        private LocalDateTime snapshotTimestamp; // 관련 시간별 스냅샷 시각
        private Double hourlyAvgTemperature;
        private Double hourlyAvgHumidity;
        private Integer hourlyAvgPressure;
        private Integer hourlyAvgTvoc;
        private Integer hourlyAvgEco2;
        private Double hourlyAvgPm10;
        private Double hourlyAvgPm25;
        private ScoreData scoreData; // 점수 데이터
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreData {
        private Double overallScore;
        private Double pm10Score;
        private Double pm25Score;
        private Double eco2Score;
        private Double tvocScore;
    }
}
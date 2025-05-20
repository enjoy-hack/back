package com.example.smartair.dto.airQualityDataDto;

import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HourlySensorAirQualitySnapshotResponse {
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime snapshotHour;

    // 해당 시간대의 대표 공기질 데이터 값들
    private Double hourlyAvgTemperature;
    private Double hourlyAvgHumidity;
    private Integer hourlyAvgPressure;
    private Integer hourlyAvgTvoc;
    private Integer hourlyAvgEco2;

    //미세먼지 데이터 대표값들
    private Double hourlyAvgPm10;
    private Double hourlyAvgPm25;

    // 해당 시간대의 대표 공기질 데이터에 대한 점수
    private Double overallScore;
    private Double pm10Score;
    private Double pm25Score;
    private Double eco2Score;
    private Double tvocScore;

    public static HourlySensorAirQualitySnapshotResponse from(HourlySensorAirQualitySnapshot snapshot) {
        return HourlySensorAirQualitySnapshotResponse.builder()
                .snapshotHour(snapshot.getSnapshotHour())
                .hourlyAvgTemperature(snapshot.getHourlyAvgTemperature())
                .hourlyAvgHumidity(snapshot.getHourlyAvgHumidity())
                .hourlyAvgPressure(snapshot.getHourlyAvgPressure())
                .hourlyAvgTvoc(snapshot.getHourlyAvgTvoc())
                .hourlyAvgEco2(snapshot.getHourlyAvgEco2())
                .hourlyAvgPm10(snapshot.getHourlyAvgPm10())
                .hourlyAvgPm25(snapshot.getHourlyAvgPm25())
                .overallScore(snapshot.getOverallScore())
                .pm10Score(snapshot.getPm10Score())
                .pm25Score(snapshot.getPm25Score())
                .eco2Score(snapshot.getEco2Score())
                .tvocScore(snapshot.getTvocScore())
                .build();
    }
}

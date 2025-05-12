package com.example.smartair.dto.airQualityScoreDto;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SensorAirQualityScoreDto {
    private LocalDateTime createdAt;
    private double overallScore;
    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;
    private Long airQualityDataId;

    public static SensorAirQualityScoreDto fromEntity(SensorAirQualityScore sensorAirQualityScore) {
        if (sensorAirQualityScore == null){
            throw new CustomException(ErrorCode.DEVICE_AIR_DATA_NOT_FOUND);
        }
        Long dataId = (sensorAirQualityScore.getSensorAirQualityData()!= null) ? sensorAirQualityScore.getSensorAirQualityData().getId() : null;

        return SensorAirQualityScoreDto.builder()
                .createdAt(sensorAirQualityScore.getCreatedAt())
                .overallScore(sensorAirQualityScore.getOverallScore())
                .pm10Score(sensorAirQualityScore.getPm10Score())
                .pm25Score(sensorAirQualityScore.getPm25Score())
                .eco2Score(sensorAirQualityScore.getEco2Score())
                .tvocScore(sensorAirQualityScore.getTvocScore())
                .airQualityDataId(dataId)
                .build();
    }

    
}

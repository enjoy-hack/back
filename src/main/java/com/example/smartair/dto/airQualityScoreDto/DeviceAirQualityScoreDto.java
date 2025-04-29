package com.example.smartair.dto.airQualityScoreDto;
import com.example.smartair.entity.airScore.DeviceAirQualityScore;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DeviceAirQualityScoreDto {
    private LocalDateTime createdAt;
    private double overallScore;
    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;
    private Long airQualityDataId;

    public static DeviceAirQualityScoreDto fromEntity(DeviceAirQualityScore deviceAirQualityScore) {
        if (deviceAirQualityScore == null){
            throw new CustomException(ErrorCode.DEVICE_AIR_DATA_NOT_FOUND);
        }
        Long dataId = (deviceAirQualityScore.getAirQualityData() != null) ? deviceAirQualityScore.getAirQualityData().getId() : null;

        return DeviceAirQualityScoreDto.builder()
                .createdAt(deviceAirQualityScore.getCreatedAt())
                .overallScore(deviceAirQualityScore.getOverallScore())
                .pm10Score(deviceAirQualityScore.getPm10Score())
                .pm25Score(deviceAirQualityScore.getPm25Score())
                .eco2Score(deviceAirQualityScore.getEco2Score())
                .tvocScore(deviceAirQualityScore.getTvocScore())
                .airQualityDataId(dataId)
                .build();
    }

    
}

package com.example.smartair.dto.customUserDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class UserSatisfactionResponseDto {
    private Long id;
    private Double satisfaction;
    private String roomId;

    private double roomAirQualityScore;

    //roomAirQualityData
    private double avgTemperature;
    private double avgHumidity;
    private double avgPressure;
    private double avgTvoc;
    private double avgEco2;
    private double avgRawh2;
    private double avgRawethanol;

    private LocalDateTime createdAt;
}

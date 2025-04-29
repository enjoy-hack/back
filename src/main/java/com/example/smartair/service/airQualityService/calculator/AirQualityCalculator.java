package com.example.smartair.service.airQualityService.calculator;

import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.entity.airScore.DeviceAirQualityScore;
import com.example.smartair.domain.enums.Pollutant;

public interface AirQualityCalculator {
    DeviceAirQualityScore calculateScore(AirQualityData airQualityData);
    double calculatePollutantScore(Pollutant pollutant, double concentration);
}

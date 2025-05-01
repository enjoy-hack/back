package com.example.smartair.service.airQualityService.calculator;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.DeviceAirQualityScore;
import com.example.smartair.domain.enums.Pollutant;

public interface AirQualityCalculator {
    DeviceAirQualityScore calculateScore(DeviceAirQualityData airQualityData);
    double calculatePollutantScore(Pollutant pollutant, double concentration);
}

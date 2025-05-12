package com.example.smartair.service.airQualityService.calculator;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.domain.enums.Pollutant;

public interface AirQualityCalculator {
    SensorAirQualityScore calculateScore(SensorAirQualityData airQualityData);
    double calculatePollutantScore(Pollutant pollutant, double concentration);
}

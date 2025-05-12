package com.example.smartair.infrastructure;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;

import java.util.Optional;

public interface RecentAirQualityDataCache {
    void put(Long deviceId, SensorAirQualityData airQualityData);
    Optional<SensorAirQualityData> get(Long deviceId);
}

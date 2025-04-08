package com.example.smartair.infrastructure;

import com.example.smartair.entity.airData.AirQualityData;

import java.util.Optional;

public interface RecentAirQualityDataCache {
    void put(Long deviceId, AirQualityData airQualityData);
    Optional<AirQualityData> get(Long deviceId);
}

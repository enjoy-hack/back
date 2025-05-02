package com.example.smartair.infrastructure;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;

import java.util.Optional;

public interface RecentAirQualityDataCache {
    void put(Long deviceId, DeviceAirQualityData airQualityData);
    Optional<DeviceAirQualityData> get(Long deviceId);
}

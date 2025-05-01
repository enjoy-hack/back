package com.example.smartair.infrastructure;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecentAirQualityDataCacheImpl implements RecentAirQualityDataCache {
    private final Map<Long, DeviceAirQualityData> cache = new ConcurrentHashMap<>();

    @Override
    public void put(Long deviceId, DeviceAirQualityData airQualityData) {
        cache.put(deviceId, airQualityData);
    }

    @Override
    public Optional<DeviceAirQualityData> get(Long deviceId){
        return Optional.ofNullable(cache.get(deviceId));
    }
}

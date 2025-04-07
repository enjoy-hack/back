package com.example.smartair.infrastructure;

import com.example.smartair.entity.airData.AirQualityData;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RecentAirQualityDataCacheImpl implements RecentAirQualityDataCache {
    private final Map<Long, AirQualityData> cache = new ConcurrentHashMap<>();

    @Override
    public void put(Long deviceId, AirQualityData airQualityData) {
        cache.put(deviceId, airQualityData);
    }

    @Override
    public Optional<AirQualityData> get(Long deviceId){
        return Optional.ofNullable(cache.get(deviceId));
    }
}

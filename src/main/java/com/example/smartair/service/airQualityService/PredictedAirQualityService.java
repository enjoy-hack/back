package com.example.smartair.service.airQualityService;

import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PredictedAirQualityService {

    private final PredictedAirQualityRepository predictedAirQualityRepository;
}

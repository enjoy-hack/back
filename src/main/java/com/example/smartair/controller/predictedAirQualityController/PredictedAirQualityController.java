package com.example.smartair.controller.predictedAirQualityController;

import com.example.smartair.dto.deviceDto.DeviceStateResponseDto;
import com.example.smartair.service.airQualityService.PredictedAirQualityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class PredictedAirQualityController {

    private final PredictedAirQualityService predictedAirQualityService;

    @PostMapping("/predictedAirQuality")
    public ResponseEntity<?> getPredictedAirQualityforSensorId(String sensorId) {
        try {
            // 예측된 공기질 데이터 가져오기
            DeviceStateResponseDto predictedAirQuality = predictedAirQualityService.getPredictedAirQuality(sensorId);
            return ResponseEntity.ok(predictedAirQuality);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error retrieving predicted air quality data");
        }
    }
}

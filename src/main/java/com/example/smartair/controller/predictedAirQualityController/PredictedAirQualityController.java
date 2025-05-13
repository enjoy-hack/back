package com.example.smartair.controller.predictedAirQualityController;

import com.example.smartair.dto.deviceDto.DeviceStateResponseDto;
import com.example.smartair.dto.predictedAirQualityDto.PredictedAirQualityDto;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.exception.CustomException;
import com.example.smartair.service.airQualityService.PredictedAirQualityService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Controller
@AllArgsConstructor
public class PredictedAirQualityController {

    private final PredictedAirQualityService predictedAirQualityService;

    @PostMapping("/predictedAirQuality")
    public ResponseEntity<?> setPredictedAirQualityforSensorId(@RequestBody List<PredictedAirQualityDto> predictedAirQualityDto) {
        // 예측된 공기질 데이터 가져오기
        predictedAirQualityService.setPredictedAirQuality(predictedAirQualityDto);
        return ResponseEntity.ok("예측된 공기질 데이터 저장 완료");

    }

    @GetMapping("/predictedAirQuality")
    public ResponseEntity<?> getPredictedAirQuality(Long sensorSerialNumber) {
        // 예측된 공기질 데이터 가져오기
        List<PredictedAirQualityData> predictedAirQualityData = predictedAirQualityService.getPredictedAirQuality(sensorSerialNumber);
        return ResponseEntity.ok(predictedAirQualityData);
    }
}

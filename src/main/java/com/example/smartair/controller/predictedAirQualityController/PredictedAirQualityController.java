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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class PredictedAirQualityController implements PredictedAirQualityControllerDocs {

    private final PredictedAirQualityService predictedAirQualityService;

    @GetMapping("/sensorMappingWithRoom")
    public ResponseEntity<?> getSensorMappingWithRoom() {

        // 센서와 방 매핑 정보 가져오기
        List<?> sensorMappingWithRoom = predictedAirQualityService.getSensorMappingWithRoom();
        if (sensorMappingWithRoom == null || sensorMappingWithRoom.isEmpty()) {
            return ResponseEntity.badRequest().body("요청 데이터가 비어있습니다.");
        }
        return ResponseEntity.ok(sensorMappingWithRoom);

        //반환 예시

    }
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
        if (predictedAirQualityData == null || predictedAirQualityData.isEmpty()) {
            return ResponseEntity.badRequest().body("요청 데이터가 비어있습니다.");
        }
        return ResponseEntity.ok(predictedAirQualityData);
    }
}

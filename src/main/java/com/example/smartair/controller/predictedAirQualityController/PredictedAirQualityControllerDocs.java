package com.example.smartair.controller.predictedAirQualityController;



import com.example.smartair.dto.predictedAirQualityDto.PredictedAirQualityDto;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "예측 공기질 API", description = "예측된 공기질 데이터를 저장하고 조회하는 API")
public interface PredictedAirQualityControllerDocs {
    @Operation(
            summary = "센서와 방 매핑 정보 조회",
            description = """
            ## 센서와 방 매핑 정보 조회
            센서와 방의 매핑 정보를 조회합니다.

            ---
            **응답 예시**
            ```json
            [
              {
                "sensorSerialNumber": 12345,
                "sensorRegisterDate": "2025-05-13T12:00:00"
              }
            ]
            ```
            """
    )ResponseEntity<?> getSensorMappingWithRoom();
    @Operation(
            summary = "예측 공기질 데이터 저장",
            description = """
            ## 예측 공기질 저장
            AI가 예측한 공기질 데이터를 여러 개 저장합니다.

            ---
            **요청 본문**
            - sensorSerialNumber (Long): 센서 일련번호
            - timestamp (String): 예측 시각 (형식: yyyy-MM-ddTHH:mm:ss)
            - pm10 (float): 예측된 PM10 농도
            - co2 (float): 예측된 CO2 농도
            - tvoc (float): 예측된 TVOC 농도

            ---
            **응답**
            - 200 OK: "예측된 공기질 데이터 저장 완료"
            """
    )
    ResponseEntity<?> setPredictedAirQualityforSensorId(@RequestBody List<PredictedAirQualityDto> predictedAirQualityDto);

    @Operation(
            summary = "예측 공기질 데이터 조회",
            description = """
            ## 예측 공기질 조회
            특정 센서 일련번호를 기반으로 시간순으로 정렬된 예측 공기질 데이터를 조회합니다.

            ---
            **요청 파라미터**
            - sensorSerialNumber (Long): 센서 일련번호

            ---
            **응답 예시**
            ```json
            [
              {
                "id": 1,
                "timestamp": "2025-05-13T12:00:00",
                "sensorSerialNumber": 12345,
                "roomId": 1001,
                "pm10": 23.5,
                "co2": 415.0,
                "tvoc": 0.33
              }
            ]
            ```
            """
    )
    ResponseEntity<?> getPredictedAirQuality(@RequestParam Long sensorSerialNumber);
}

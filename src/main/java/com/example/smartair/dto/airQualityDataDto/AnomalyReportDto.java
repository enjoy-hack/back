package com.example.smartair.dto.airQualityDataDto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyReportDto {
    private Long sensorSerialNumber; // 센서 ID
    private String anomalyTimestamp; // 이상치 발생 시각
    private String pollutant; // 예: "TVOC", "PM10", "CO2"
    private Double pollutantValue; // 이상치 값
    private Double predictedValue; // 예측된 값
}

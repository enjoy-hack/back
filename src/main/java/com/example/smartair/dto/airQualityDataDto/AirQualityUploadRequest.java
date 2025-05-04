package com.example.smartair.dto.airQualityDataDto;

import lombok.Data;

@Data
public class AirQualityUploadRequest {
    private String deviceId;       // 기기 ID
    private String jsonPayload;    // 실제 JSON 문자열 ({"pm25":20,...})
}


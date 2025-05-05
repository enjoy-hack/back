package com.example.smartair.dto.mqttMessageDto;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqttMessageRequestDto {
    private String topic;
    private String payload;
}

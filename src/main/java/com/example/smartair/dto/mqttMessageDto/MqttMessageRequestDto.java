package com.example.smartair.dto.mqttMessageDto;

import lombok.Data;

@Data
public class MqttMessageRequestDto {
    private String topic;
    private String payload;
}

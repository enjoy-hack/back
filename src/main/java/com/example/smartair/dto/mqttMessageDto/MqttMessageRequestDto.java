package com.example.smartair.dto.mqttMessageDto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MqttMessageRequestDto {
    private String topic;
    private String payload;
}

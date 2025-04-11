package com.example.smartair.dto.AirQualityDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityPayloadDto {
    private double temperature;
    private double humidity;
    private int pressure;
    private int tvoc;
    private int ppm;
    private int rawh2;
    private int rawethanol;
}

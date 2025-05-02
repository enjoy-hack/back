package com.example.smartair.dto.airQualityDataDto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("pt1_pm10_standard")
    private double pt1Pm10Standard;
    @JsonProperty("pt1_pm25_standard")
    private double pt1Pm25Standard;
    @JsonProperty("pt1_pm100_standard")
    private double pt1Pm100Standard;
    @JsonProperty("pt1_particles_03um")
    private int pt1Particles03um;
    @JsonProperty("pt1_particles_05um")
    private int pt1Particles05um;
    @JsonProperty("pt1_particles_10um")
    private int pt1Particles10um;
    @JsonProperty("pt1_particles_25um")
    private int pt1Particles25um;
    @JsonProperty("pt1_particles_50um")
    private int pt1Particles50um;
    @JsonProperty("pt1_particles_100um")
    private int pt1Particles100um;

    @JsonProperty("pt2_pm10_standard")
    private double pt2Pm10Standard;
    @JsonProperty("pt2_pm25_standard")
    private double pt2Pm25Standard;
    @JsonProperty("pt2_pm100_standard")
    private double pt2Pm100Standard;
    @JsonProperty("pt2_particles_03um")
    private int pt2Particles03um;
    @JsonProperty("pt2_particles_05um")
    private int pt2Particles05um;
    @JsonProperty("pt2_particles_10um")
    private int pt2Particles10um;
    @JsonProperty("pt2_particles_25um")
    private int pt2Particles25um;
    @JsonProperty("pt2_particles_50um")
    private int pt2Particles50um;
    @JsonProperty("pt2_particles_100um")
    private int pt2Particles100um;
}

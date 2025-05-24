package com.example.smartair.dto.airQualityDataDto;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityDataResponse {
    private Long id;
    private double temperature;
    private double humidity;
    private int pressure;

    private int tvoc;
    private int eco2;
    private int rawh2;
    private int rawethanol;

    private double pt1_pm10_standard;
    private double pt1_pm25_standard;
    private double pt1_pm100_standard;

    private int pt1_particle_03;
    private int pt1_particle_05;
    private int pt1_particle_10;
    private int pt1_particle_25;
    private int pt1_particle_50;
    private int pt1_particle_100;

    private double pt2_pm10_standard;
    private double pt2_pm25_standard;
    private double pt2_pm100_standard;

    private int pt2_particle_03;
    private int pt2_particle_05;
    private int pt2_particle_10;
    private int pt2_particle_25;
    private int pt2_particle_50;
    private int pt2_particle_100;

    public static AirQualityDataResponse from(SensorAirQualityData data) {
        AirQualityDataResponseBuilder builder = AirQualityDataResponse.builder();

        // SensorAirQualityData가 null이 아닌 경우
        if (data != null) {
            builder.id(data.getId())
                    .temperature(data.getTemperature())
                    .humidity(data.getHumidity())
                    .pressure(data.getPressure())
                    .tvoc(data.getTvoc())
                    .eco2(data.getEco2())
                    .rawh2(data.getRawh2())
                    .rawethanol(data.getRawethanol());

            // FineParticlesData가 존재할 경우
            if (data.getFineParticlesData() != null) {
                builder
                        .pt1_pm10_standard(data.getFineParticlesData().getPm10_standard())
                        .pt1_pm25_standard(data.getFineParticlesData().getPm25_standard())
                        .pt1_pm100_standard(data.getFineParticlesData().getPm100_standard())
                        .pt1_particle_03(data.getFineParticlesData().getParticle_03())
                        .pt1_particle_05(data.getFineParticlesData().getParticle_05())
                        .pt1_particle_10(data.getFineParticlesData().getParticle_10())
                        .pt1_particle_25(data.getFineParticlesData().getParticle_25())
                        .pt1_particle_50(data.getFineParticlesData().getParticle_50())
                        .pt1_particle_100(data.getFineParticlesData().getParticle_100());
            }

            // FineParticlesDataPt2가 존재할 경우
            if (data.getFineParticlesDataPt2() != null) {
                builder
                        .pt2_pm10_standard(data.getFineParticlesDataPt2().getPm10_standard())
                        .pt2_pm25_standard(data.getFineParticlesDataPt2().getPm25_standard())
                        .pt2_pm100_standard(data.getFineParticlesDataPt2().getPm100_standard())
                        .pt2_particle_03(data.getFineParticlesDataPt2().getParticle_03())
                        .pt2_particle_05(data.getFineParticlesDataPt2().getParticle_05())
                        .pt2_particle_10(data.getFineParticlesDataPt2().getParticle_10())
                        .pt2_particle_25(data.getFineParticlesDataPt2().getParticle_25())
                        .pt2_particle_50(data.getFineParticlesDataPt2().getParticle_50())
                        .pt2_particle_100(data.getFineParticlesDataPt2().getParticle_100());
            }
        }

        return builder.build();
    }


}

package com.example.smartair.dto.airQualityDataDto;

import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AirQualityPayloadDto {
    private Double temperature;
    @JsonProperty("hum")
    private Double humidity;
    private Integer pressure;
    private Integer tvoc;
    private Integer eco2;
    private Integer rawh2;
    private Integer rawethanol;

    @JsonProperty("pt1")
    private PtData pt1;

    @JsonProperty("pt2")
    private PtData pt2;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PtData {
        @JsonProperty("pm10_standard")
        private double pm10Standard;
        @JsonProperty("pm25_standard")
        private double pm25Standard;
        @JsonProperty("pm100_standard")
        private double pm100Standard;
        @JsonProperty("particles_03um")
        private int particles03um;
        @JsonProperty("particles_05um")
        private int particles05um;
        @JsonProperty("particles_10um")
        private int particles10um;
        @JsonProperty("particles_25um")
        private int particles25um;
        @JsonProperty("particles_50um")
        private int particles50um;
        @JsonProperty("particles_100um")
        private int particles100um;
    }

    public FineParticlesData toPt1Entity() {
        return FineParticlesData.builder()
                .pm10_standard(pt1.getPm10Standard())
                .pm25_standard(pt1.getPm25Standard())
                .pm100_standard(pt1.getPm100Standard())
                .particle_03(pt1.getParticles03um())
                .particle_05(pt1.getParticles05um())
                .particle_10(pt1.getParticles10um())
                .particle_25(pt1.getParticles25um())
                .particle_50(pt1.getParticles50um())
                .particle_100(pt1.getParticles100um())
                .build();
    }

    public FineParticlesDataPt2 toPt2Entity() {
        return FineParticlesDataPt2.builder()
                .pm10_standard(pt2.getPm10Standard())
                .pm25_standard(pt2.getPm25Standard())
                .pm100_standard(pt2.getPm100Standard())
                .particle_03(pt2.getParticles03um())
                .particle_05(pt2.getParticles05um())
                .particle_10(pt2.getParticles10um())
                .particle_25(pt2.getParticles25um())
                .particle_50(pt2.getParticles50um())
                .particle_100(pt2.getParticles100um())
                .build();
    }
}

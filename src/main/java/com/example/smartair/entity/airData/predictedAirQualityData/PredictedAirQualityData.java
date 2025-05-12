package com.example.smartair.entity.airData.predictedAirQualityData;

import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;

@Entity
@Builder
@Table(name = "predicted_air_quality_data")
public class PredictedAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "timestamp")
    private String timestamp;
    @Column(name = "sensor_id")
    private Long sensorSerialNumber;
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "pm10")
    private float pm100;
    @Column(name = "co2")
    private float co2;
    @Column(name = "tvoc")
    private float tvoc;


    public PredictedAirQualityData(Long sensorSerialNumber, String timestamp, float pm100, float co2, float tvoc) {
        this.sensorSerialNumber = sensorSerialNumber;
        this.timestamp = timestamp;
        this.pm100 = pm100;
        this.co2 = co2;
        this.tvoc = tvoc;
    }
}

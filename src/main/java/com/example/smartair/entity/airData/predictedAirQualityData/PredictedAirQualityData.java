package com.example.smartair.entity.airData.predictedAirQualityData;

import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "predicted_air_quality_data")
public class PredictedAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "timestamp")
    private String timestamp;
    @Column(name = "sensor_id")
    private Long sensorId;
    @Column(name = "room_id")
    private Long roomId;
    @Column(name = "pm10")
    private float pm100;
    @Column(name = "co2")
    private float co2;
    @Column(name = "tvoc")
    private float tvoc;



}

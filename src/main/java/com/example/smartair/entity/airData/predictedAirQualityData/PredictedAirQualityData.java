package com.example.smartair.entity.airData.predictedAirQualityData;

import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "predicted_air_quality_data")
public class PredictedAirQualityData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "sensor_id")
    private String sensorSerialNumber;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "pm10")
    private float pm10;

    @Column(name = "co2")
    private float co2;

    @Column(name = "tvoc")
    private float tvoc;
}

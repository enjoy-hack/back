package com.example.smartair.entity.airData.airQualityData;

import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "sensor_air_quality_data")
public class SensorAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperature; //온도
    private double humidity; //습도
    private int pressure;

    private int tvoc;
    private int eco2;
    private int rawh2;
    private int rawethanol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "fine_particles_data_id") // 외래 키 컬럼명 지정
    private FineParticlesData fineParticlesData;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "fine_particles_data_pt2_id") // 외래 키 컬럼명 지정
    private FineParticlesDataPt2 fineParticlesDataPt2;

    @OneToOne
    @JoinColumn(name = "predictedAirQuality_data_id")
    private PredictedAirQualityData predictedAirQualityData;
}

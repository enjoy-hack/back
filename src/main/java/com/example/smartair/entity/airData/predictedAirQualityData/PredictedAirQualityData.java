package com.example.smartair.entity.airData.predictedAirQualityData;

import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "predicted_air_quality_data")
public class PredictedAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}

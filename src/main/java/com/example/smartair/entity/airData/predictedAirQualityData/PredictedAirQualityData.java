package com.example.smartair.entity.airData.predictedAirQualityData;

import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
public class PredictedAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

}

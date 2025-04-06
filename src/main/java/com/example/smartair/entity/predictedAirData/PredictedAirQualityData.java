package com.example.smartair.entity.predictedAirData;

import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
public class PredictedAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "airQualityData_id")
    private AirQualityData airQualityData;
}

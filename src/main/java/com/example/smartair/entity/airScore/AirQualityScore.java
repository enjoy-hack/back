package com.example.smartair.entity.airScore;

import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
public class AirQualityScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private AirQualityData airQualityData;

    private double score;

    private double pm10Score;
    private double pm25Score;
    private double tvocScore;
}

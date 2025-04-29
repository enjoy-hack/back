package com.example.smartair.entity.airScore;

import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class DeviceAirQualityScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "air_quality_data_id")
    private AirQualityData airQualityData;

    private double overallScore;

    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;


}

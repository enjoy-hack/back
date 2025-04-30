package com.example.smartair.entity.airData.airQualityData;

import com.example.smartair.entity.place.Place;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //공간의 공기질 데이터와 공간 - 다대일
    @JoinColumn(name = "place_id")
    private Place place;

    private double avgTemperature;
    private double avgHumidity;
    private double avgPressure;
    private double avgTvoc;
    private double avgEco2;
    private double avgRawh2;
    private double avgRawethanol;

}

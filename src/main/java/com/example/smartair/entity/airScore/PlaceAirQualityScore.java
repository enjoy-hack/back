package com.example.smartair.entity.airScore;

import com.example.smartair.entity.place.Place;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlaceAirQualityScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double overallScore;
    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
}

package com.example.smartair.entity.user;

import com.example.smartair.entity.airData.airQualityData.RoomAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserSatisfaction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "room_air_quality_score_id")
    private RoomAirQualityScore roomAirQualityScore;
    @ManyToOne
    @JoinColumn(name = "air_quality_data")
    private RoomAirQualityData roomAirQualityData;

    private Double satisfaction;

}

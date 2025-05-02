package com.example.smartair.entity.airData.airQualityData;

import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomAirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne //방의 공기질 데이터와 방 - 다대일
    @JoinColumn(name = "room_id")
    private Room room;

    private double avgTemperature;
    private double avgHumidity;
    private double avgPressure;
    private double avgTvoc;
    private double avgEco2;
    private double avgRawh2;
    private double avgRawethanol;

   

}

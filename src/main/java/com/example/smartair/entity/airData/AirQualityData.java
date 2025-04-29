package com.example.smartair.entity.airData;

import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.predictedAirData.PredictedAirQualityData;
import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class AirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private String payload;

    private double temperature; //온도
    private double humidity; //습도
    private int pressure;

    private int tvoc;
    private int eco2;
    private int rawh2;
    private int rawethanol;

    @ManyToOne //공기질 데이터와 기기 : 다대일
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne //공기질 데이터와 방 : 다대일
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToOne
    @JoinColumn(name = "fineParticlesData_id") //공기질 데이터와 미세먼지 데이터 : 일대일
    private FineParticlesData fineParticlesData;

    @OneToOne
    @JoinColumn(name = "predictedAirQuality_id")
    private PredictedAirQualityData predictedAirQualityData;
}

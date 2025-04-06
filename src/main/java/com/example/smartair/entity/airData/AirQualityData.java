package com.example.smartair.entity.airData;

import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.notification.Notification;
import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String topic;
    private String payload;

    private double temperature;
    private double humidity;
    private int pressure;

    private int tvoc;
    private int ppm;
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

    @OneToMany
    private List<Notification> notificationList = new ArrayList<>();
}

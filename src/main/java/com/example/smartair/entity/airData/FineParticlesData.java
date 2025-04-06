package com.example.smartair.entity.airData;

import com.example.smartair.entity.device.Device;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
public class FineParticlesData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int pm10_standard;
    private int pm25_standard;
    private int pm100_standard;

    private int particle_03;
    private int particle_05;
    private int particle_10;
    private int particle_25;
    private int particle_50;
    private int particle_100;

    @OneToOne(mappedBy = "fineParticlesData")
    private AirQualityData airQuality;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;
}

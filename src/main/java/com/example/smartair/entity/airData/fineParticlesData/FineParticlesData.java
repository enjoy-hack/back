package com.example.smartair.entity.airData.fineParticlesData;

import com.example.smartair.entity.device.Device;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineParticlesData extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double pm10_standard;
    private double pm25_standard;
    private double pm100_standard;

    private int particle_03;
    private int particle_05;
    private int particle_10;
    private int particle_25;
    private int particle_50;
    private int particle_100;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;
}

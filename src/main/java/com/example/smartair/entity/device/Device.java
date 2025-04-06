package com.example.smartair.entity.device;

import com.example.smartair.entity.hvacSetting.HvacSetting;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.entity.user.User;
import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.entity.airData.FineParticlesData;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Device extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String serialNumber;
    private boolean runningStatus;

    @ManyToOne //기기와 유저 : 다대일 관계
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "device")
    private List<RoomDevice> roomDevices = new ArrayList<>();

    @OneToMany(mappedBy = "device")
    private List<HvacSetting> hvacSettings = new ArrayList<>();

    @OneToMany
    private List<AirQualityData> airQualityDataList = new ArrayList<>();

    @OneToMany
    private List<FineParticlesData> fineParticlesDataList = new ArrayList<>();
}

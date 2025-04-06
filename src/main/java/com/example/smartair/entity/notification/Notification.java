package com.example.smartair.entity.notification;

import com.example.smartair.entity.user.User;
import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;

@Entity
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String title;
    private boolean readStatus;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "airQualityData_id")
    private AirQualityData airQualityData;
}

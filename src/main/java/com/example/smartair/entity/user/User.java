package com.example.smartair.entity.user;

import com.example.smartair.entity.BaseTimeEntity;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.hvacSetting.HvacSetting;
import com.example.smartair.entity.login.Role;
import com.example.smartair.entity.notification.Notification;
import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;
}

package com.example.smartair.entity.user;

import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.hvacSetting.HvacSetting;
import com.example.smartair.entity.notification.Notification;
import com.example.smartair.entity.room.Room;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    private String role;

}


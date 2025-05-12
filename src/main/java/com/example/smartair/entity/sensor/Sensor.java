package com.example.smartair.entity.sensor;

import com.example.smartair.entity.user.User;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sensor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long serialNumber;
    private boolean runningStatus;
    private String currentValue;

    @ManyToOne //기기와 유저 : 다대일 관계
    @JoinColumn(name = "user_id")
    private User user;

}

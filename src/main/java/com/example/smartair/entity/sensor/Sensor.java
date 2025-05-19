package com.example.smartair.entity.sensor;

import com.example.smartair.entity.user.User;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Sensor extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique = true)
    private String serialNumber;
    private boolean runningStatus;
    private boolean isRegistered; //방 등록 여부
    private LocalDateTime roomRegisterDate; //센서 방 등록 날짜

    @ManyToOne //기기와 유저 : 다대일 관계
    @JoinColumn(name = "user_id")
    private User user;

}

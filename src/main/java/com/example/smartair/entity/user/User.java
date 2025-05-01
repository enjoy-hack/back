package com.example.smartair.entity.user;

import com.example.smartair.entity.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password; // local 로그인 사용자만 저장

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20) // 또는 더 넉넉하게 30 정도
    private Role role;

    private String loginType; //로그인 타입

}

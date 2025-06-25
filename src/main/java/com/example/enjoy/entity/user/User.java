package com.example.enjoy.entity.user;

import com.example.enjoy.entity.BaseTimeEntity;

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

    private String loginType; //로그인 타입

    private String providerId; //소셜 로그인 시 제공자 ID



}

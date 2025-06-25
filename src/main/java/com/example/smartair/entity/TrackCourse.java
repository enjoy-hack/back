package com.example.smartair.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class TrackCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String courseName;  // 현재 과목명
    private String courseAlias; // 구(과거) 과목명

    @ManyToOne
    @JoinColumn(name = "track_id")
    private Track track;
    // Getters and Setters
}

package com.example.enjoy.entity;

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
    private String courseCode;       // 과목 코드 (DTO의 'code'에 해당)
    private String academicYear;     // 이수 추천 학년 (DTO의 'year'에 해당)
    private String academicSemester; // 이수 추천 학기 (DTO의 'semester'에 해당)

    @ManyToOne
    @JoinColumn(name = "track_id")
    private Track track;
}

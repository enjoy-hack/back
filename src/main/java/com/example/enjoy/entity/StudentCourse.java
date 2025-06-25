package com.example.enjoy.entity;

import com.example.enjoy.dto.StudentCourseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudentCourse extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String courseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentCourseStatus status;

    @Column(nullable = false)
    private boolean manual;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public void updateStatus(StudentCourseStatus status) {
        this.status = status;
    }

}
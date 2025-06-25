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

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String major;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String completedSemester;

    public void updateUserInfo(String studentId, String username, String major, String grade, String completedSemester) {
        this.studentId = studentId;
        this.username = username;
        this.major = major;
        this.grade = grade;
        this.completedSemester = completedSemester;
    }

}

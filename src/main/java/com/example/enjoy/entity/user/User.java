package com.example.enjoy.entity.user;

import com.example.enjoy.entity.BaseTimeEntity;

import com.example.enjoy.entity.UserTrack;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserTrack> userTracks = new ArrayList<>();

    public void updateUserInfo(String studentId, String username, String major, String grade, String completedSemester) {
        this.studentId = studentId;
        this.username = username;
        this.major = major;
        this.grade = grade;
        this.completedSemester = completedSemester;
    }

}

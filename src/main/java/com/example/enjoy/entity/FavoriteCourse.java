package com.example.enjoy.entity;

import com.example.enjoy.entity.user.User;
import com.example.enjoy.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class FavoriteCourse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "course_name")
    private String courseName;

    public FavoriteCourse() {

    }

    public FavoriteCourse(User user, String courseName) {
        this.user = user;
        this.courseName = courseName;
    }
}

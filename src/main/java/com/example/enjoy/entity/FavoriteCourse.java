package com.example.enjoy.entity;

import com.example.enjoy.entity.user.User;
import com.example.enjoy.util.BaseEntity;
import jakarta.persistence.*;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
public class FavoriteCourse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "track_course_id")
    private TrackCourse trackCourse;

}

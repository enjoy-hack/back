package com.example.enjoy.repository;

import com.example.enjoy.entity.FavoriteCourse;
import com.example.enjoy.entity.TrackCourse;
import com.example.enjoy.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteCourseRepository extends JpaRepository<FavoriteCourse, Long> {
    Optional<FavoriteCourse> findByUserAndCourseName(User user, String courseName);
    List<FavoriteCourse> findAllByUser(User user);
}

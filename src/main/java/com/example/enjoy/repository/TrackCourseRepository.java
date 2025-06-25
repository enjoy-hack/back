package com.example.enjoy.repository;

import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.TrackCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackCourseRepository extends JpaRepository<TrackCourse, Long> {

    List<TrackCourse> findAllByTrack(Track track);

    Optional<TrackCourse> findByCourseName(String courseName);
}
package com.example.smartair.repository;

import com.example.smartair.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {

    @Query("SELECT DISTINCT t FROM Track t LEFT JOIN FETCH t.courses")
    List<Track> findAllWithCourses();
}

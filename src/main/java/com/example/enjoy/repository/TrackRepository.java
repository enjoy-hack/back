package com.example.enjoy.repository;

import com.example.enjoy.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    @Query("SELECT DISTINCT t FROM Track t LEFT JOIN FETCH t.courses")
    List<Track> findAllWithCourses();

    /**
     * 특정 ID의 트랙을 과목 정보와 함께 조회합니다 (N+1 문제 해결).
     * @param trackId 트랙 ID
     * @return 트랙 정보 (Optional)
     */
    @Query("SELECT t FROM Track t JOIN FETCH t.courses WHERE t.id = :trackId")
    Optional<Track> findByIdWithCourses(@Param("trackId") Long trackId);

    Optional<Track> findByName(String name);

}
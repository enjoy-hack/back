package com.example.smartair.repository.airQualityScoreRepository;

import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomAirQualityScoreRepository extends JpaRepository<RoomAirQualityScore, Long> {
    Optional<RoomAirQualityScore> findFirstByRoomOrderByCreatedAtDesc(Room room); // Spring Data JPA 이름 규칙 활용

    // Place에 속한 모든 RoomAirQualityScore 찾기
    List<RoomAirQualityScore> findByRoom_Place(Place place);

    // Room ID와 시간 범위로 RoomAirQualityScore 목록을 페이징하여 조회 
    @Query("SELECT rqs FROM RoomAirQualityScore rqs WHERE rqs.room.id = :roomId " +
           "AND (:startTime IS NULL OR rqs.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR rqs.createdAt <= :endTime)")
    Page<RoomAirQualityScore> findScoresByRoomIdAndTimeRange(
            @Param("roomId") Long roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

} 
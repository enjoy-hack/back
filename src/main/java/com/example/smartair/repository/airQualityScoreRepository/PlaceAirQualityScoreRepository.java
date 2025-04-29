package com.example.smartair.repository.airQualityScoreRepository;

import com.example.smartair.entity.airScore.PlaceAirQualityScore;
import com.example.smartair.entity.place.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PlaceAirQualityScoreRepository extends JpaRepository<PlaceAirQualityScore, Long> {
    Optional<PlaceAirQualityScore> findFirstByPlaceOrderByCreatedAtDesc(Place place);

    // Place ID와 시간 범위로 PlaceAirQualityScore 목록을 페이징하여 조회
    @Query("SELECT pas FROM PlaceAirQualityScore pas WHERE pas.place.id = :placeId " +
           "AND (:startTime IS NULL OR pas.createdAt >= :startTime) " +
           "AND (:endTime IS NULL OR pas.createdAt <= :endTime)")
    Page<PlaceAirQualityScore> findScoresByPlaceIdAndTimeRange(
            @Param("placeId") Long placeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );
} 
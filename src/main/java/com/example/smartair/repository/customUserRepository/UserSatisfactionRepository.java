package com.example.smartair.repository.customUserRepository;

import com.example.smartair.entity.user.UserSatisfaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSatisfactionRepository extends JpaRepository<UserSatisfaction, Long> {
    @Query("SELECT us FROM UserSatisfaction us " +
            "WHERE us.roomAirQualityScore.room.id = :roomId " +
            "ORDER BY us.createdAt DESC")
    List<UserSatisfaction> findTop7ByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId);

}

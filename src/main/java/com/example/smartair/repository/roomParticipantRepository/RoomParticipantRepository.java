package com.example.smartair.repository.roomParticipantRepository;

import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    Boolean existsByRoomAndUser(Room room, User user);
    Optional<RoomParticipant> findByRoomAndUser(Room room, User user);
    List<RoomParticipant> findByRoom(Room room);


}

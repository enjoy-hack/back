package com.example.smartair.repository.roomRepository;

import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
//    List<Room> findAllByPlace(Place place);
    Optional<Room> findRoomById(Long roomId);

    boolean existsByIdAndParticipants_User(Long roomId, User user);

}

package com.example.smartair.repository.roomRepository;

import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
//    List<Room> findAllByPlace(Place place);
    Optional<Room> findRoomById(Long roomId);

}

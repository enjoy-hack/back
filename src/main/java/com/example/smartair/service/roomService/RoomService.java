package com.example.smartair.service.roomService;

import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponse;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.PlaceRepository;
import com.example.smartair.repository.RoomRepository;
import com.example.smartair.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PlaceRepository placeRepository;

    public RoomService(UserRepository userRepository, RoomRepository roomRepository, PlaceRepository placeRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.placeRepository = placeRepository;
    }

    /**
     * room 생성
     */
    @Transactional
    public RoomDetailResponse createRoom(Long userId, CreateRoomRequestDto createRoomRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));

        Room room = Room.builder()
                .name(createRoomRequestDto.getName())
                .place(createRoomRequestDto.getPlace())
                .user(user)
                .build();

        Room savedRoom = roomRepository.save(room);

        return RoomDetailResponse.from(savedRoom);
    }

    /**
     * room 수정
     */
    public RoomDetailResponse updateRoom(Long userId, Long roomId, CreateRoomRequestDto createRoomRequestDto) {
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("User not found"));

        room.setName(createRoomRequestDto.getName());
        room.setPlace(createRoomRequestDto.getPlace());
        room.setUser(user);

        roomRepository.save(room);

         return RoomDetailResponse.from(room);
    }

    /**
     * room 삭제
     */
    public void deleteRoom(Long roomId){
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        roomRepository.delete(room);
    }

    /**
     * room 상세 정보 조회
     */
    public RoomDetailResponse getRoomDetail(Long roomId){
        Room room = roomRepository.findById(roomId).orElseThrow(()->new RuntimeException("Room not found"));
        return RoomDetailResponse.from(room);
    }

    /**
     * place의 모든 room 목록 조회
     */
    public List<RoomDetailResponse> getAllRoomsByPlace(Long placeId){
        Place place = placeRepository.findById(placeId).orElseThrow(()->new RuntimeException("Place not found"));

        List<Room> rooms = roomRepository.findAllByPlace(place);

        return rooms.stream()
                .map(RoomDetailResponse::from)
                .collect(Collectors.toList());
    }

}

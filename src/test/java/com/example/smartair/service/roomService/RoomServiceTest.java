//package com.example.smartair.service.roomService;
//
//import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
//import com.example.smartair.dto.roomDto.RoomDetailResponse;
//import com.example.smartair.entity.place.Place;
//import com.example.smartair.entity.room.Room;
//import com.example.smartair.entity.user.User;
//import com.example.smartair.repository.placeRepository.PlaceRepository;
//import com.example.smartair.repository.roomRepository.RoomRepository;
//import com.example.smartair.repository.userRepository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class RoomServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private RoomRepository roomRepository;
//
//    @Mock
//    private PlaceRepository placeRepository;
//
//    @InjectMocks
//    private RoomService roomService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    @DisplayName("방 생성에 성공")
//    void createRoom(){
//        //given
//        User user = User.builder().id(1L).build();
//        Place place = Place.builder().id(1L).name("내 집").build();
//
//        CreateRoomRequestDto data = CreateRoomRequestDto.builder()
//                .name("거실")
//                .place(place)
//                .build();
//
//        Room room = Room.builder()
//                .id(1L)
//                .name("거실")
//                .place(place)
//                .user(user)
//                .build();
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(roomRepository.save(any(Room.class))).thenReturn(room);
//
//        //when
//        RoomDetailResponse roomDetailResponse = roomService.createRoom(1L, data);
//
//        //then
//        assertNotNull(roomDetailResponse);
//        assertEquals("거실", roomDetailResponse.getName());
//        assertEquals("내 집", roomDetailResponse.getPlace().getName());
//    }
//
//    @Test
//    @DisplayName("방 수정에 성공")
//    void updateRoom(){
//        //given
//        User user = User.builder().id(1L).build();
//        Place place = Place.builder().id(1L).name("학교").build();
//
//        CreateRoomRequestDto data = CreateRoomRequestDto.builder()
//                .name("수정된 방 이름")
//                .place(place)
//                .build();
//
//        Room room = Room.builder()
//                .id(1L)
//                .name("수정되기 전 방 이름")
//                .place(place)
//                .user(user)
//                .build();
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//        when(roomRepository.save(any(Room.class))).thenReturn(room);
//
//        //when
//        RoomDetailResponse roomDetailResponse = roomService.updateRoom(1L, 1L, data);
//
//        //then
//        assertNotNull(roomDetailResponse);
//        assertEquals("수정된 방 이름", roomDetailResponse.getName());
//        assertEquals("학교", roomDetailResponse.getPlace().getName());
//    }
//
//    @Test
//    @DisplayName("방 삭제에 성공")
//    void deleteRoom(){
//        //given
//        Room room = Room.builder().id(1L).build();
//
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//        doNothing().when(roomRepository).delete(room);
//
//        //when
//        roomService.deleteRoom(1L);
//
//        //then
//        verify(roomRepository).findById(1L);
//        verify(roomRepository).delete(room);
//    }
//
//    @Test
//    @DisplayName("방 상세조회에 성공")
//    void getRoomDetail(){
//        //given
//        Place place = Place.builder().id(1L).name("학교").build();
//        User user = User.builder().id(1L).build();
//
//        Room room = Room.builder()
//                .id(1L)
//                .name("거실")
//                .place(place)
//                .owner(user)
//                .build();
//
//        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
//
//        //when
//        RoomDetailResponse roomDetailResponse = roomService.getRoomDetail(1L);
//
//        //then
//        assertNotNull(roomDetailResponse);
//        assertEquals("거실", roomDetailResponse.getName());
//        assertEquals("학교", roomDetailResponse.getPlaceId().getName());
//    }
//
//    @Test
//    @DisplayName("특정 place의 방 목록 조회에 성공")
//    void getAllRoomsByPlace(){
//        //given
//        Long placeId = 1L;
//        Place place = Place.builder().id(placeId).name("내 집").build();
//
//        Room room1 = Room.builder().id(1L).name("거실").place(place).build();
//        Room room2 = Room.builder().id(2L).name("안방").place(place).build();
//
//        List<Room> roomList = List.of(room1, room2);
//
//        when(roomRepository.findAllByPlace(place)).thenReturn(roomList);
//        when(placeRepository.findById(placeId)).thenReturn(Optional.of(place));
//
//        //when
//        List<RoomDetailResponse> roomDetailResponseList = roomService.getAllRoomsByPlace(placeId);
//
//        //then
//        assertNotNull(roomDetailResponseList);
//        assertThat(roomDetailResponseList).hasSize(2);
//        assertThat(roomDetailResponseList.get(0).getName()).isEqualTo("거실");
//        assertThat(roomDetailResponseList.get(1).getName()).isEqualTo("안방");
//
//        verify(roomRepository).findAllByPlace(place);
//        verify(placeRepository).findById(placeId);
//    }
//
//
//}
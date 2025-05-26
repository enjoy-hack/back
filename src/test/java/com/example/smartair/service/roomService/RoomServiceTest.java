package com.example.smartair.service.roomService;


import com.example.smartair.dto.roomDto.CreateRoomRequestDto;
import com.example.smartair.dto.roomDto.JoinRoomRequestDto;
import com.example.smartair.dto.roomDto.RoomDetailResponseDto;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomParticipant.PatPermissionRequestStatus;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.user.Role;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.roomParticipantRepository.RoomParticipantRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.checkerframework.checker.units.qual.N;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomParticipantRepository roomParticipantRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private RoomService roomService;

    private User adminUser;
    private User managerUser;
    private User normalUser;
    private RoomParticipant normalRoomParticipant;
    private CreateRoomRequestDto createRoomRequestDto;
    private Room room;
    private JoinRoomRequestDto joinRoomRequestDto;
    private JoinRoomRequestDto wrongJoinRoomRequestDto;
    private Message message;

    @BeforeEach
    void setUpTestData() {

        MockitoAnnotations.openMocks(this);

        adminUser = User.builder()
                .id(1L)
                .fcmToken("admin-fcm-token")
                .role(Role.ADMIN).build();

        managerUser = User.builder()
                .id(2L)
                .fcmToken("manager-fcm-token")
                .role(Role.MANAGER).build();

        normalUser = User.builder()
                .id(3L)
                .fcmToken("test-fcm-token")
                .role(Role.USER).build();

        normalRoomParticipant = RoomParticipant.builder()
                .user(normalUser)
                .roleInRoom(Role.USER)
                .patPermissionRequestStatus(PatPermissionRequestStatus.NONE)
                .room(room)
                .build();

        createRoomRequestDto = CreateRoomRequestDto.builder()
                .name("Test Room")
                .password("testPassword")
                .deviceControlEnabled(false)
                .latitude(10.0)
                .longitude(20.0)
                .build();

        joinRoomRequestDto = JoinRoomRequestDto.builder()
                .password("testPassword")
                .build();

        wrongJoinRoomRequestDto = JoinRoomRequestDto.builder()
                .password("wrongPassword")
                .build();

        room = Room.builder()
                .name("Test Room")
                .password("testPassword")
                .deviceControlEnabled(true)
                .latitude(10.0)
                .longitude(20.0)
                .owner(managerUser)
                .build();

        message = Message.builder()
                .setToken("test-fcm-token")
                .putData("type", "PERMISSION_REJECTED")
                .putData("message", "사용자 ID " + normalRoomParticipant.getUser().getUsername() + "의 장치 제어 권한 요청이 거절되었습니다.")
                .setNotification(Notification.builder()
                        .setTitle("권한 거절")
                        .setBody("제어 권한 요청이 거절되었습니다.")
                        .build())
                .build();

    }

    @Nested
    @DisplayName("방 생성 시나리오")
    class CreateRoomTests {

        @Test
        @DisplayName("admin 유저가 방을 생성할 때 성공")
        void testCreateRoomByAdmin() {
            // given
            when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
            when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
                Room savedRoom = invocation.getArgument(0);
                assertThat(savedRoom.getOwner()).isEqualTo(adminUser);
                assertThat(savedRoom.getName()).isEqualTo(createRoomRequestDto.getName());
                assertThat(savedRoom.getPassword()).isEqualTo(createRoomRequestDto.getPassword());
                assertThat(savedRoom.isDeviceControlEnabled()).isEqualTo(createRoomRequestDto.isDeviceControlEnabled());
                assertThat(savedRoom.getLatitude()).isEqualTo(createRoomRequestDto.getLatitude());
                assertThat(savedRoom.getLongitude()).isEqualTo(createRoomRequestDto.getLongitude());
                return savedRoom;
            });

            when(roomParticipantRepository.save(any())).thenAnswer(invocation -> {
                RoomParticipant savedParticipant = invocation.getArgument(0);
                assertThat(savedParticipant.getUser()).isEqualTo(adminUser);
                return savedParticipant;
            });

            // when
            RoomDetailResponseDto resultDto = roomService.createRoom(adminUser.getId(), createRoomRequestDto);

            // then
            assertThat(resultDto).isNotNull();
            assertThat(resultDto.getName()).isEqualTo(createRoomRequestDto.getName());
            verify(userRepository).findById(adminUser.getId());
            verify(roomRepository).save(any());
            verify(roomParticipantRepository).save(any());
        }

        @Test
        @DisplayName("manager 유저가 방을 생성할 때 성공")
        void testCreateRoomByManager() {
            // given
            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
            when(roomRepository.save(any())).thenAnswer(invocation -> {
                Room savedRoom = invocation.getArgument(0);
                assertThat(savedRoom.getOwner()).isEqualTo(managerUser);
                assertThat(savedRoom.getName()).isEqualTo(createRoomRequestDto.getName());
                assertThat(savedRoom.getPassword()).isEqualTo(createRoomRequestDto.getPassword());
                assertThat(savedRoom.isDeviceControlEnabled()).isEqualTo(createRoomRequestDto.isDeviceControlEnabled());
                assertThat(savedRoom.getLatitude()).isEqualTo(createRoomRequestDto.getLatitude());
                assertThat(savedRoom.getLongitude()).isEqualTo(createRoomRequestDto.getLongitude());
                return savedRoom;
            });
            when(roomParticipantRepository.save(any())).thenAnswer(invocation -> {
                RoomParticipant savedParticipant = invocation.getArgument(0);
                assertThat(savedParticipant.getUser()).isEqualTo(managerUser);
                return savedParticipant;
            });

            // when
            RoomDetailResponseDto resultDto = roomService.createRoom(managerUser.getId(), createRoomRequestDto);

            // then
            assertThat(resultDto).isNotNull();
            assertThat(resultDto.getName()).isEqualTo(createRoomRequestDto.getName());
            verify(userRepository).findById(managerUser.getId());
            verify(roomRepository).save(any());
            verify(roomParticipantRepository).save(any());
        }

       

        @Test
        @DisplayName("존재하지 않는 유저로 방을 생성할 때 예외 발생")
        void testCreateRoomWithNonExistentUser() {
            // given
            Long nonExistentUserId = 999L;
            when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> roomService.createRoom(nonExistentUserId, createRoomRequestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            verify(userRepository).findById(nonExistentUserId);
            verify(roomRepository, never()).save(any());
            verify(roomParticipantRepository, never()).save(any());
        }

    }

    @Nested
    @DisplayName("방 참여 시나리오")
    class JoinRoomTests {

        @Test
        @DisplayName("일반 유저가 방에 참여할 때 성공")
        void testUserJoinSuccess() {
            //given
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(any())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.save(any(RoomParticipant.class))).thenAnswer(invocation ->
            {
                RoomParticipant savedParticipant = invocation.getArgument(0);
                assertThat(savedParticipant.getUser()).isEqualTo(normalUser);
                assertThat(savedParticipant.getRoleInRoom()).isEqualTo(Role.USER);
                assertThat(savedParticipant.getRoom()).isEqualTo(room);
                return savedParticipant;
            });

            //when
            RoomDetailResponseDto resultDto = roomService.joinRoom(normalUser.getId(), room.getId(), joinRoomRequestDto);

            //then
            //room의 participants set에 추가되었는지 검증
            verify(roomParticipantRepository).save(argThat(participant -> {
                boolean isUserMatch = participant.getUser().equals(normalUser);
                boolean isRoomMatch = participant.getRoom().equals(room);
                return isRoomMatch && isUserMatch;
            }));

            //room의 participants set 검증
            assertThat(room.getParticipants()).hasSize(1);
            assertThat(room.getParticipants()).extracting("user").contains(normalUser);

            //DTO 검증
            assertThat(resultDto).isNotNull();
            assertThat(resultDto.getName()).isEqualTo(room.getName());
        }

        @Test
        @DisplayName("방장이 자신의 방에 참여할 때 예외 발생")
        void testJoinOwner() {
            //given
            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

            //when & then
            assertThatThrownBy(() -> roomService.joinRoom(managerUser.getId(), room.getId(), joinRoomRequestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OWNER_CANNOT_JOIN_OWN_ROOM);
        }

        @Test
        @DisplayName("방 비밀번호가 틀렸을 때 예외 발생")
        void testWrongPassword() {
            //given
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

            //when & then
            assertThatThrownBy(() -> roomService.joinRoom(normalUser.getId(), room.getId(), wrongJoinRoomRequestDto))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_ROOM_PASSWORD);
        }

    }

    @Nested
    @DisplayName("PAT 장치 제어 권한 관리 시나리오")
    class UpdateParticipantDeviceControlTests {

        @Test
        @DisplayName("방장이 방 참여자의 기기 제어 권한을 변경할 때 성공")
        void testUpdateParticipantDeviceControlByOwner() {
            // given
            normalRoomParticipant.setCanControlPatDevices(false);
            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.findByRoomAndUser(room, normalUser)).thenReturn(Optional.of(normalRoomParticipant));

            // when
            roomService.updateParticipantDeviceControl(managerUser.getId(), room.getId(), normalUser.getId(), true);

            // then
            verify(userRepository).findById(managerUser.getId());
            assertThat(normalRoomParticipant.getCanControlPatDevices()).isEqualTo(true);
        }

//        @Test
//        @DisplayName("방장이 PAT 장치 제어 권한 요청을 승인할 때 성공")
//        void testApprovePatDeviceControlRequestByOwner() throws FirebaseMessagingException {
//            // given
//            room.addParticipant(normalRoomParticipant);
//            normalRoomParticipant.setPatPermissionRequestStatus(PatPermissionRequestStatus.PENDING);
//            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
//            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
//            when(roomParticipantRepository.findById(normalRoomParticipant.getId())).thenReturn(Optional.of(normalRoomParticipant));
//
//            // when
//            String result = roomService.approvePatDeviceControlPermission(managerUser.getId(), normalRoomParticipant.getId());
//
//            //then
//            assertThat(normalRoomParticipant.getCanControlPatDevices()).isEqualTo(true);
//            assertThat(normalRoomParticipant.getPatPermissionRequestStatus()).isEqualTo(PatPermissionRequestStatus.APPROVED);
//            verify(roomParticipantRepository).save(normalRoomParticipant);
//            assertThat(result).isNotNull();
//        }

//        @Test
//        @DisplayName("방장이 PAT 장치 제어 권한 요청을 거부할 때 성공")
//        void testDenyPatDeviceControlRequestByOwner() throws FirebaseMessagingException {
//            // given
//            room.addParticipant(normalRoomParticipant);
//            normalRoomParticipant.setPatPermissionRequestStatus(PatPermissionRequestStatus.PENDING);
//            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
//            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
//            when(roomParticipantRepository.findById(normalRoomParticipant.getId())).thenReturn(Optional.of(normalRoomParticipant));
//
//            // when
//            String result = roomService.rejectPatDeviceControlPermission(managerUser.getId(), normalRoomParticipant.getId());
//
//            //then
//            assertThat(normalRoomParticipant.getCanControlPatDevices()).isEqualTo(false);
//            assertThat(normalRoomParticipant.getPatPermissionRequestStatus()).isEqualTo(PatPermissionRequestStatus.REJECTED);
//            verify(roomParticipantRepository).save(normalRoomParticipant);
//            assertThat(result).isNotNull();
//            verify(firebaseMessaging).send(any(Message.class));
//        }

        @Test
        @DisplayName("일반 유저가 방 참여자의 기기 권한을 관리할 때 예외 발생")
        void testUpdateParticipantDeviceControlByNormalUser() {
            // given
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

            // when & then
            assertThatThrownBy(() -> roomService.updateParticipantDeviceControl(normalUser.getId(), room.getId(), normalUser.getId(), true))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITY_TO_MANAGE_PARTICIPANTS);
        }

        @Test
        @DisplayName("방장이 본인의 기기 권한을 관리할 때 예외 발생")
        void testUpdateOwnerDeviceControl() {
            // given
            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.findByRoomAndUser(room, managerUser)).thenReturn(Optional.of(normalRoomParticipant));

            // when & then
            assertThatThrownBy(() -> roomService.updateParticipantDeviceControl(managerUser.getId(), room.getId(), managerUser.getId(), true))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_CHANGE_OWNER_DEVICE_CONTROL);
        }
    }

    @Nested
    @DisplayName("방 참여자 강퇴 시나리오")
    class RemoveParticipantsFromRoomTests {

        @Test
        @DisplayName("방장이 방 참여자를 강퇴할 때 성공")
        void testRemoveParticipantsFromRoom() {
            //given
            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.findByRoomAndUser(room, normalUser)).thenReturn(Optional.of(normalRoomParticipant));

            //when
            roomService.removeParticipantFromRoom(managerUser.getId(), room.getId(), normalUser.getId());

            //then
            verify(roomParticipantRepository).delete(normalRoomParticipant);
            assertThat(room.getParticipants()).doesNotContain(normalRoomParticipant);
        }

        @Test
        @DisplayName("관리자가 방 참여자를 강퇴할 때 성공")
        void testRemoveParticipantsFromRoomByManager() {
            //given
            when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.findByRoomAndUser(room, normalUser)).thenReturn(Optional.of(normalRoomParticipant));

            //when
            roomService.removeParticipantFromRoom(adminUser.getId(), room.getId(), normalUser.getId());

            //then
            verify(roomParticipantRepository).delete(normalRoomParticipant);
            assertThat(room.getParticipants()).doesNotContain(normalRoomParticipant);
        }

        @Test
        @DisplayName("일반 유저가 방 참여자를 강퇴할 때 예외 발생")
        void testRemoveParticipantsFromRoomByNormalUser() {
            //given
            User targetUser = User.builder().id(4L).role(Role.USER).build();
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));

            //when & then
            assertThatThrownBy(() -> roomService.removeParticipantFromRoom(normalUser.getId(), room.getId(), targetUser.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NO_AUTHORITY_TO_MANAGE_PARTICIPANTS);
        }

        @Test
        @DisplayName("방장이 본인을 강퇴할 때 예외 발생")
        void testRemoveOwnerFromRoom() {
            //given
            RoomParticipant ownerRoomParticipant = RoomParticipant.builder()
                    .user(managerUser)
                    .roleInRoom(Role.MANAGER)
                    .build();
            when(userRepository.findById(managerUser.getId())).thenReturn(Optional.of(managerUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.findByRoomAndUser(room, managerUser)).thenReturn(Optional.of(ownerRoomParticipant));

            //when & then
            assertThatThrownBy(() -> roomService.removeParticipantFromRoom(managerUser.getId(), room.getId(), managerUser.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CANNOT_REMOVE_OWNER_FROM_ROOM);
        }
    }

    @Nested
    @DisplayName("PAT 장치 제어 권한 요청 시나리오")
    class RequestPatDeviceControlPermissionTests {
//        @Test
//        @DisplayName("일반 유저가 PAT 장치 제어 권한 요청 성공")
//        void testRequestPatDeviceControlPermission() throws FirebaseMessagingException {
//            //given
//            room.setDeviceControlEnabled(false);
//            normalRoomParticipant.setCanControlPatDevices(false);
//            room.addParticipant(normalRoomParticipant);
//            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
//            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
//            when(roomParticipantRepository.findByRoomAndUser(room, normalUser)).thenReturn(Optional.of(normalRoomParticipant));
//
//            //when
//            String result = roomService.requestPatDeviceControlPermission(normalUser.getId(), room.getId());
//
//            //then
//            assertThat(normalRoomParticipant.getPatPermissionRequestStatus()).isEqualTo(PatPermissionRequestStatus.PENDING);
//            verify(roomParticipantRepository).save(normalRoomParticipant);
//            assertThat(result).isNotNull();
//            verify(firebaseMessaging).send(any(Message.class));
//        }

        @Test
        @DisplayName("이미 권한을 가지고 있을 때 예외 발생")
        void testRequestPatDeviceControlPermissionAlreadyGranted() {
            //given
            room.setDeviceControlEnabled(false);
            normalRoomParticipant.setCanControlPatDevices(true);
            when(userRepository.findById(normalUser.getId())).thenReturn(Optional.of(normalUser));
            when(roomRepository.findById(room.getId())).thenReturn(Optional.of(room));
            when(roomParticipantRepository.findByRoomAndUser(room, normalUser)).thenReturn(Optional.of(normalRoomParticipant));

            //when & then
            assertThatThrownBy(() -> roomService.requestPatDeviceControlPermission(normalUser.getId(), room.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAT_PERMISSION_REQUEST_ALREADY_EXISTS);

        }
    }
}
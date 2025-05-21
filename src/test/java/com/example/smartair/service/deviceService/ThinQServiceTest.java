package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceDto;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThinQServiceTest {

    private ThinQService thinQService;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private RoomRepository roomRepository;

    private User mockUser;
    private Room mockRoom;
    private Room mockRoom2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        thinQService = new ThinQService(null, null, null, null, deviceRepository, roomRepository, null, "", "", "", "");
        mockUser = User.builder().id(1L).username("testuser").build();
        mockRoom = Room.builder().id(1L).owner(mockUser).build();
        mockRoom2 = Room.builder().id(2L).owner(mockUser).build();
    }

    @Test
    void getDeviceList_RoomNotFound_ThrowsException() {
        when(roomRepository.findRoomById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> {
            thinQService.getDeviceList(mockUser, 1L);
        });

        assertEquals("방을 찾을 수 없습니다.", exception.getMessage());
        verify(roomRepository, times(1)).findRoomById(1L);
    }

    @Test
    void updateDevice_ValidRequest_UpdatesDevice() {
        Device mockDevice = Device.builder().id(1L).room(mockRoom).build();

        when(roomRepository.findRoomById(1L)).thenReturn(Optional.of(mockRoom));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(mockDevice));
        when(roomRepository.findRoomById(2L)).thenReturn(Optional.of(mockRoom2));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(mockDevice));

        DeviceDto result = (DeviceDto) thinQService.updateDevice(mockUser, 2L, 1L);

        assertNotNull(result);
        assertEquals(2L, result.getRoomId());
        verify(deviceRepository, times(1)).save(mockDevice);
    }
}
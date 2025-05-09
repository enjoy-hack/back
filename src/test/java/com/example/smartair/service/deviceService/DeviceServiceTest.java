package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceRequestDto;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.roomDeviceRepository.RoomDeviceRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class DeviceServiceTest {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoomDeviceRepository roomDeviceRepository;

    private User testUser;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        // 사용자 생성
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("secure")
                .role(com.example.smartair.entity.user.Role.MANAGER)
                .build();
        userRepository.save(testUser);

        // 방 생성
        testRoom = Room.builder()
                .name("Test Room")
                .user(testUser)
                .build();
        roomRepository.save(testRoom);
    }

    @Test
    void 디바이스_등록_후_조회() throws Exception {
        // given
        DeviceRequestDto.setDeviceDto dto = new DeviceRequestDto.setDeviceDto(1001L, "TestDevice", testRoom.getId());

        // when
        deviceService.setDevice(testUser, dto);

        // then
        List<Device> devices = deviceService.getDevices(testRoom.getId());
        assertThat(devices).hasSize(1);
        assertThat(devices.get(0).getSerialNumber()).isEqualTo(1001L);
        assertThat(devices.get(0).getName()).isEqualTo("TestDevice");
    }

    @Test
    void 디바이스_삭제() throws Exception {
        // given
        DeviceRequestDto.setDeviceDto dto = new DeviceRequestDto.setDeviceDto(2002L, "ToDeleteDevice", testRoom.getId());
        deviceService.setDevice(testUser, dto);

        DeviceRequestDto.deleteDeviceDto deleteDto = new DeviceRequestDto.deleteDeviceDto(2002L, testRoom.getId());

        // when
        deviceService.deleteDevice(testUser, deleteDto);

        // then
        List<Device> devices = deviceService.getDevices(testRoom.getId());
        assertThat(devices).isEmpty();
    }

    @Test
    void 디바이스_상태_확인() throws Exception {
        // given
        Long serialNumber = 3003L;
        DeviceRequestDto.setDeviceDto dto = new DeviceRequestDto.setDeviceDto(serialNumber, "StatusCheck", testRoom.getId());
        deviceService.setDevice(testUser, dto);

        // when
        boolean status = deviceService.getDeviceStatus(serialNumber);

        // then
        assertThat(status).isFalse(); // 등록 시 runningStatus = false
    }
}

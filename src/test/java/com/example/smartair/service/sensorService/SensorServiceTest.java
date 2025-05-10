package com.example.smartair.service.sensorService;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
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
public class SensorServiceTest {

    @Autowired
    private SensorService sensorService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private RoomSensorRepository roomSensorRepository;

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
        SensorRequestDto.setDeviceDto dto = new SensorRequestDto.setDeviceDto(1001L, "TestDevice", testRoom.getId());

        // when
        sensorService.setDevice(testUser, dto);

        // then
        List<Sensor> sensors = sensorService.getDevices(testRoom.getId());
        assertThat(sensors).hasSize(1);
        assertThat(sensors.get(0).getSerialNumber()).isEqualTo(1001L);
        assertThat(sensors.get(0).getName()).isEqualTo("TestDevice");
    }

    @Test
    void 디바이스_삭제() throws Exception {
        // given
        SensorRequestDto.setDeviceDto dto = new SensorRequestDto.setDeviceDto(2002L, "ToDeleteDevice", testRoom.getId());
        sensorService.setDevice(testUser, dto);

        SensorRequestDto.deleteDeviceDto deleteDto = new SensorRequestDto.deleteDeviceDto(2002L, testRoom.getId());

        // when
        sensorService.deleteDevice(testUser, deleteDto);

        // then
        List<Sensor> sensors = sensorService.getDevices(testRoom.getId());
        assertThat(sensors).isEmpty();
    }

    @Test
    void 디바이스_상태_확인() throws Exception {
        // given
        Long serialNumber = 3003L;
        SensorRequestDto.setDeviceDto dto = new SensorRequestDto.setDeviceDto(serialNumber, "StatusCheck", testRoom.getId());
        sensorService.setDevice(testUser, dto);

        // when
        boolean status = sensorService.getDeviceStatus(serialNumber);

        // then
        assertThat(status).isFalse(); // 등록 시 runningStatus = false
    }
}

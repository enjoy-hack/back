package com.example.smartair.service.sensorService;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.SensorAirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.WeeklySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlySensorAirQualitySnapshotRepository;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomSensorRepository roomSensorRepository;

    @Mock
    private FineParticlesDataRepository fineParticlesDataRepository;

    @Mock
    private FineParticlesDataPt2Repository fineParticlesDataPt2Repository;

    @Mock
    private SensorAirQualityScoreRepository sensorAirQualityScoreRepository;

    @Mock
    private DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;

    @Mock
    private WeeklySensorAirQualityReportRepository weeklySensorAirQualityReportRepository;

    @Mock
    private PredictedAirQualityRepository predictedAirQualityRepository;

    @Mock
    private SensorAirQualityDataRepository sensorAirQualityDataRepository;

    @Mock
    private HourlySensorAirQualitySnapshotRepository hourlySensorAirQualitySnapshotRepository;
    @InjectMocks
    private SensorService sensorService;

    private User testUser;
    private Room testRoom;
    private Sensor testSensor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .name("Test Room")
                .owner(testUser)
                .build();

        testSensor = Sensor.builder()
                .id(1L)
                .serialNumber("12345")
                .name("Test Sensor")
                .user(testUser)
                .isRegistered(false)
                .runningStatus(false)
                .build();
    }

    @Test
    void setSensor_성공() throws Exception {
        SensorRequestDto.setSensorDto dto = new SensorRequestDto.setSensorDto("12345", "Test Sensor");

        when(sensorRepository.save(any(Sensor.class))).thenReturn(testSensor);

        Sensor result = sensorService.setSensor(testUser, dto);

        assertNotNull(result);
        assertEquals(dto.serialNumber(), result.getSerialNumber());
        assertEquals(dto.name(), result.getName());
        verify(sensorRepository, times(1)).save(any(Sensor.class));
    }

    @Test
    void addSensorToRoom_성공() throws Exception {
        SensorRequestDto.addSensorToRoomDto dto = new SensorRequestDto.addSensorToRoomDto("12345", 1L);

        when(roomRepository.findRoomById(dto.roomId())).thenReturn(Optional.of(testRoom));
        when(sensorRepository.findBySerialNumber(dto.serialNumber())).thenReturn(Optional.of(testSensor));
        when(roomRepository.existsByIdAndParticipants_User(testRoom.getId(), testUser)).thenReturn(true);
        when(roomSensorRepository.existsBySensor_SerialNumberAndRoom_Id(dto.serialNumber(), testRoom.getId())).thenReturn(false);

        RoomSensor result = sensorService.addSensorToRoom(testUser, dto);

        assertNotNull(result);
        assertEquals(testRoom, result.getRoom());
        assertEquals(testSensor, result.getSensor());
        verify(sensorRepository, times(1)).save(testSensor);
        verify(roomSensorRepository, times(1)).save(any(RoomSensor.class));
    }

    @Test
    void deleteSensor_성공() throws Exception {

        RoomSensor roomSensor = RoomSensor.builder()
                .sensor(testSensor)
                .room(testRoom)
                .build();

        when(sensorRepository.findBySerialNumber(testSensor.getSerialNumber())).thenReturn(Optional.of(testSensor));
        when(roomRepository.findRoomById(anyLong())).thenReturn(Optional.of(testRoom));
        when(roomRepository.existsByIdAndParticipants_User(testRoom.getId(), testUser)).thenReturn(true);
        when(roomSensorRepository.findAllBySensor_Id(testSensor.getId())).thenReturn(Collections.singletonList(roomSensor));

        sensorService.deleteSensor(testUser, testSensor.getSerialNumber());

        verify(sensorRepository, times(1)).delete(testSensor);
        verify(roomSensorRepository, times(1)).deleteAll(Collections.singletonList(roomSensor));
    }

    @Test
    void getSensorStatus_성공() throws Exception {
        when(sensorRepository.findBySerialNumber(testSensor.getSerialNumber())).thenReturn(Optional.of(testSensor));

        boolean status = sensorService.getSensorStatus(testSensor.getSerialNumber());

        assertFalse(status);
        verify(sensorRepository, times(1)).findBySerialNumber(testSensor.getSerialNumber());
    }

    @Test
    void unregitsterSensorFromRoom_성공() throws Exception {
        RoomSensor roomSensor = RoomSensor.builder()
                .sensor(testSensor)
                .room(testRoom)
                .build();

        when(roomRepository.findRoomById(anyLong())).thenReturn(Optional.of(testRoom));
        when(roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(testSensor.getSerialNumber(), testRoom.getId()))
                .thenReturn(Optional.of(roomSensor));

        sensorService.unregisterSensorFromRoom(testUser, testSensor.getSerialNumber(), testRoom.getId());

        verify(roomSensorRepository, times(1)).delete(roomSensor);
    }
}
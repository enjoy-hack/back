package com.example.smartair.service.airQualityService.airQualityDataTest;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.SensorAirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.service.airQualityService.AirQualityDataService;
import com.example.smartair.service.airQualityService.AirQualityScoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


class AirQualityDataServiceTest {

    @Mock
    private AirQualityScoreService airQualityScoreService;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private RoomSensorRepository roomSensorRepository;

    @Mock
    private SensorAirQualityDataRepository sensorAirQualityDataRepository;

    @Mock
    private RecentAirQualityDataCache recentAirQualityDataCache;

    @Mock
    private FineParticlesDataRepository fineParticlesDataRepository;

    @Mock
    private FineParticlesDataPt2Repository fineParticlesDataPt2Repository;

    @InjectMocks
    private AirQualityDataService airQualityDataService;

    private static final Long TEST_SENSOR_ID = 1L;
    private static final Long TEST_ROOM_ID = 1L;
    private static final String TEST_TOPIC = "smartair/" +  +TEST_SENSOR_ID + "/" + TEST_ROOM_ID + "/airQuality";
    private AirQualityPayloadDto testPayloadDto;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        AirQualityPayloadDto.PtData pt1Data = AirQualityPayloadDto.PtData.builder()
                .pm10Standard(22)
                .pm25Standard(37)
                .pm100Standard(39)
                .particles03um(3867)
                .particles05um(1132)
                .particles10um(283)
                .particles25um(15)
                .particles50um(2)
                .particles100um(1)
                .build();

        AirQualityPayloadDto.PtData pt2Data = AirQualityPayloadDto.PtData.builder()
                .pm10Standard(18)
                .pm25Standard(29)
                .pm100Standard(36)
                .particles03um(3468)
                .particles05um(940)
                .particles10um(191)
                .particles25um(22)
                .particles50um(10)
                .particles100um(4)
                .build();

        testPayloadDto = AirQualityPayloadDto.builder()
                .temperature(29.47)
                .humidity(38.22)
                .pressure(399)
                .tvoc(6)
                .eco2(400)
                .rawh2(12656)
                .rawethanol(1647)
                .pt1(pt1Data)
                .pt2(pt2Data)
                .build();
    }

    @Test
    @DisplayName("유효한 topic과 payload를 받을 때 : AirQualityData 저장 및 캐싱 성공")
    void processAirQualityData(){
        //given
        Sensor sensor = Sensor.builder().id(TEST_SENSOR_ID).build();
        Room room  = Room.builder().id(TEST_ROOM_ID).build();
        RoomSensor roomSensor = RoomSensor.builder()
                .id(1L)
                .room(room)
                .sensor(sensor)
                .build();
        FineParticlesData mockSavedFineParticles = FineParticlesData.builder().id(200L).sensor(sensor).build();
        when(fineParticlesDataRepository.save(any(FineParticlesData.class))).thenReturn(mockSavedFineParticles);

        when(sensorRepository.findById(TEST_SENSOR_ID)).thenReturn(Optional.of(sensor));
        when(roomSensorRepository.findBySensor(sensor)).thenReturn(Optional.of(roomSensor));
        when(sensorAirQualityDataRepository.save(any(SensorAirQualityData.class))).thenAnswer(invocation -> {
            SensorAirQualityData savedData = invocation.getArgument(0);
            savedData.setId(100L);
            return savedData;
        });

        //when
        AirQualityPayloadDto data = airQualityDataService.processAirQualityData(TEST_SENSOR_ID, TEST_ROOM_ID, testPayloadDto);

        //then
        assertNotNull(data);
        assertEquals(29.47, data.getTemperature());
        assertEquals(38.22, data.getHumidity());
        verify(fineParticlesDataRepository).save(any(FineParticlesData.class));
        verify(sensorAirQualityDataRepository).save(any(SensorAirQualityData.class));
        verify(recentAirQualityDataCache).put(eq(sensor.getId()), any(SensorAirQualityData.class));
    }

    @Test
    @DisplayName("디바이스가 존재하지 않을 때 : CustomException(DEVICE_NOT_FOUND) 발생")
    void processAirQualityData_DeviceNotFound_ShouldThrowException(){
        //given
        when(sensorRepository.findById(TEST_SENSOR_ID)).thenReturn(Optional.empty());

        //when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            airQualityDataService.processAirQualityData(TEST_SENSOR_ID, TEST_ROOM_ID, testPayloadDto);
        });
        assertEquals(ErrorCode.SENSOR_NOT_FOUND, exception.getErrorCode());

        verify(fineParticlesDataRepository, never()).save(any());
        verify(sensorAirQualityDataRepository, never()).save(any());
        verify(recentAirQualityDataCache, never()).put(any(), any());
    }

    @Test
    @DisplayName("방 매핑이 없는 센서의 데이터 처리 : 정상 처리 확인")
    void processAirQualityData_WithNoRoomMapping_ShouldProcessSuccessfully() {
        //given
        Sensor sensor = Sensor.builder().id(TEST_SENSOR_ID).build();
        FineParticlesData mockSavedFineParticles = FineParticlesData.builder()
                .id(200L)
                .sensor(sensor)
                .build();

        when(sensorRepository.findById(TEST_SENSOR_ID)).thenReturn(Optional.of(sensor));
        when(roomSensorRepository.findBySensor(sensor)).thenReturn(Optional.empty());
        when(fineParticlesDataRepository.save(any(FineParticlesData.class))).thenReturn(mockSavedFineParticles);
        when(sensorAirQualityDataRepository.save(any(SensorAirQualityData.class))).thenAnswer(invocation -> {
            SensorAirQualityData savedData = invocation.getArgument(0);
            savedData.setId(100L);
            return savedData;
        });

        /// when
        AirQualityPayloadDto result = airQualityDataService.processAirQualityData(TEST_SENSOR_ID, null, testPayloadDto);

        // then
        assertNotNull(result);
        verify(roomSensorRepository, never()).findBySensor(any()); // roomId가 null이므로 호출되지 않아야 함
        verify(airQualityScoreService).calculateAndSaveDeviceScore(any());
        verify(fineParticlesDataRepository).save(any());
        verify(fineParticlesDataPt2Repository).save(any());
    }
}
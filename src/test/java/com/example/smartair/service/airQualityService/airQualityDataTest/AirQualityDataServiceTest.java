package com.example.smartair.service.airQualityService.airQualityDataTest;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
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
    private AirQualityDataRepository airQualityDataRepository;

    @Mock
    private RecentAirQualityDataCache recentAirQualityDataCache;

    @Mock
    private FineParticlesDataRepository fineParticlesDataRepository;

    @Mock
    private FineParticlesDataPt2Repository fineParticlesDataPt2Repository;

    @InjectMocks
    private AirQualityDataService airQualityDataService;

    private static final Long TEST_DEVICE_ID = 1L;
    private static final Long TEST_ROOM_ID = 1L;
    private static final String TEST_TOPIC = "smartair/" +  + TEST_DEVICE_ID + "/" + TEST_ROOM_ID + "/airQuality";
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
        Sensor sensor = Sensor.builder().id(TEST_DEVICE_ID).build();
        Room room  = Room.builder().id(TEST_ROOM_ID).build();
        RoomSensor roomSensor = RoomSensor.builder()
                .id(1L)
                .room(room)
                .sensor(sensor)
                .build();
        FineParticlesData mockSavedFineParticles = FineParticlesData.builder().id(200L).sensor(sensor).build();
        when(fineParticlesDataRepository.save(any(FineParticlesData.class))).thenReturn(mockSavedFineParticles);

        when(sensorRepository.findById(TEST_DEVICE_ID)).thenReturn(Optional.of(sensor));
        when(roomSensorRepository.findBySensor(sensor)).thenReturn(Optional.of(roomSensor));
        when(airQualityDataRepository.save(any(SensorAirQualityData.class))).thenAnswer(invocation -> {
            SensorAirQualityData savedData = invocation.getArgument(0);
            savedData.setId(100L);
            return savedData;
        });

        //when
        AirQualityPayloadDto data = airQualityDataService.processAirQualityData(TEST_DEVICE_ID, TEST_ROOM_ID, testPayloadDto);

        //then
        assertNotNull(data);
        assertEquals(29.47, data.getTemperature());
        assertEquals(38.22, data.getHumidity());
        verify(fineParticlesDataRepository).save(any(FineParticlesData.class));
        verify(airQualityDataRepository).save(any(SensorAirQualityData.class));
        verify(recentAirQualityDataCache).put(eq(sensor.getId()), any(SensorAirQualityData.class));
    }

    @Test
    @DisplayName("디바이스가 존재하지 않을 때 : CustomException(DEVICE_NOT_FOUND) 발생")
    void processAirQualityData_DeviceNotFound_ShouldThrowException(){
        //given
        when(sensorRepository.findById(TEST_DEVICE_ID)).thenReturn(Optional.empty());

        //when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            airQualityDataService.processAirQualityData(TEST_DEVICE_ID, TEST_ROOM_ID, testPayloadDto);
        });
        assertEquals(ErrorCode.DEVICE_NOT_FOUND, exception.getErrorCode());

        verify(fineParticlesDataRepository, never()).save(any());
        verify(airQualityDataRepository, never()).save(any());
        verify(recentAirQualityDataCache, never()).put(any(), any());
    }

    @Test
    @DisplayName("방-디바이스 매핑이 존재하지 않을 때 : CustomException(ROOM_DEVICE_MAPPING_NOT_FOUND) 발생")
    void processAirQualityData_RoomDeviceMappingNotFound_ShouldThrowException(){
        //given
        Sensor sensor = Sensor.builder().id(TEST_DEVICE_ID).build();

        when(sensorRepository.findById(TEST_DEVICE_ID)).thenReturn(Optional.of(sensor));
        when(roomSensorRepository.findBySensor(sensor)).thenReturn(Optional.empty());

        //when & then: 예외 발생 및 내용을 명확히 검증
        CustomException thrownException = assertThrows(
                CustomException.class,
                () -> airQualityDataService.processAirQualityData(TEST_DEVICE_ID, TEST_ROOM_ID, testPayloadDto),
                "ROOM_DEVICE_MAPPING_NOT_FOUND 예외가 발생해야 합니다."
        );

        // 발생한 예외의 ErrorCode 검증
        assertNotNull(thrownException, "예외가 발생해야 합니다.");
        // 에러 코드를 ROOM_DEVICE_MAPPING_NOT_FOUND로 변경
        assertEquals(ErrorCode.ROOM_DEVICE_MAPPING_NOT_FOUND, thrownException.getErrorCode(), "에러 코드가 ROOM_DEVICE_MAPPING_NOT_FOUND여야 합니다.");

        // 예외 발생 시 이후 로직은 실행되지 않음 검증
        verify(fineParticlesDataRepository, never()).save(any()); // fineParticlesData 저장 안 됨
        verify(airQualityDataRepository, never()).save(any());
        verify(recentAirQualityDataCache, never()).put(any(), any());
    }

}
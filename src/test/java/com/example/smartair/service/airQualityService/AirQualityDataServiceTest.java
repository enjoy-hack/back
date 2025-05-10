package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.Sensor.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomDevice;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
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
    private static final String TEST_TOPIC = "smartair/" +  + TEST_DEVICE_ID + "/" + TEST_ROOM_ID;
    private AirQualityPayloadDto testPayloadDto;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        testPayloadDto = AirQualityPayloadDto.builder()
                .temperature(25.5)
                .humidity(60.0)
                .pressure(100)
                .tvoc(50)
                .ppm(450)
                .rawh2(10)
                .rawethanol(5)
                .pt1Pm10Standard(10.0).pt1Pm25Standard(20.0).pt1Pm100Standard(30.0)
                .pt1Particles03um(100).pt1Particles05um(200).pt1Particles10um(300)
                .pt1Particles25um(400).pt1Particles50um(500).pt1Particles100um(600)
                .pt2Pm10Standard(11.0).pt2Pm25Standard(21.0).pt2Pm100Standard(31.0)
                .pt2Particles03um(101).pt2Particles05um(201).pt2Particles10um(301)
                .pt2Particles25um(401).pt2Particles50um(501).pt2Particles100um(601)
                .build();
    }

    @Test
    @DisplayName("유효한 topic과 payload를 받을 때 : AirQualityData 저장 및 캐싱 성공")
    void processAirQualityData(){
        //given
        Device device = Device.builder().id(TEST_DEVICE_ID).build();
        Room room  = Room.builder().id(TEST_ROOM_ID).build();
        RoomDevice roomDevice = RoomDevice.builder()
                .id(1L)
                .room(room)
                .device(device)
                .build();
        FineParticlesData mockSavedFineParticles = FineParticlesData.builder().id(200L).device(device).build();
        when(fineParticlesDataRepository.save(any(FineParticlesData.class))).thenReturn(mockSavedFineParticles);

        when(sensorRepository.findById(TEST_DEVICE_ID)).thenReturn(Optional.of(device));
        when(roomSensorRepository.findByDevice(device)).thenReturn(Optional.of(roomDevice));
        when(airQualityDataRepository.save(any(DeviceAirQualityData.class))).thenAnswer(invocation -> {
            DeviceAirQualityData savedData = invocation.getArgument(0);
            savedData.setId(100L);
            return savedData;
        });

        //when
        AirQualityPayloadDto data = airQualityDataService.processAirQualityData(TEST_TOPIC, testPayloadDto);

        //then
        assertNotNull(data);
        assertEquals(25.5, data.getTemperature());
        assertEquals(60, data.getHumidity());
        verify(fineParticlesDataRepository).save(any(FineParticlesData.class));
        verify(airQualityDataRepository).save(any(DeviceAirQualityData.class));
        verify(recentAirQualityDataCache).put(eq(device.getId()), any(DeviceAirQualityData.class));
    }

    @Test
    @DisplayName("디바이스가 존재하지 않을 때 : CustomException(DEVICE_NOT_FOUND) 발생")
    void processAirQualityData_DeviceNotFound_ShouldThrowException(){
        //given
        when(sensorRepository.findById(TEST_DEVICE_ID)).thenReturn(Optional.empty());

        //when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            airQualityDataService.processAirQualityData(TEST_TOPIC, testPayloadDto);
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
        Device device = Device.builder().id(TEST_DEVICE_ID).build();

        when(sensorRepository.findById(TEST_DEVICE_ID)).thenReturn(Optional.of(device));
        when(roomSensorRepository.findByDevice(device)).thenReturn(Optional.empty());

        //when & then: 예외 발생 및 내용을 명확히 검증
        CustomException thrownException = assertThrows(
                CustomException.class,
                () -> airQualityDataService.processAirQualityData(TEST_TOPIC, testPayloadDto),
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
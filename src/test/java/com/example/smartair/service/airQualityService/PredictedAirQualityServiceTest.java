package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.predictedAirQualityDto.PredictedAirQualityDto;
import com.example.smartair.dto.roomSensorDto.SensorRoomMappingDto;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PredictedAirQualityServiceTest {

    @Mock
    private PredictedAirQualityRepository predictedAirQualityRepository;

    @Mock
    private RoomSensorRepository roomSensorRepository;

    @InjectMocks
    private PredictedAirQualityService predictedAirQualityService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getSensorMappingWithRoom_ReturnsMappedData() {
        // Mock 데이터 생성
        Sensor sensor1 = Sensor.builder()
                .serialNumber("12345")
                .id(1L)
                .name("Sensor1")
                .roomRegisterDate(LocalDateTime.of(2025, 3, 15, 10, 30))
                .build();
        Sensor sensor2 = Sensor.builder()
                .serialNumber("67890")
                .id(2L)
                .name("Sensor2")
                .roomRegisterDate(LocalDateTime.of(2025, 3, 16, 14, 45))
                .build();

        RoomSensor roomSensor1 = new RoomSensor();
        roomSensor1.setSensor(sensor1);
        roomSensor1.setRoom(new Room());
        roomSensor1.getRoom().setId(1L);
        roomSensor1.getRoom().setName("Room1");

        RoomSensor roomSensor2 = new RoomSensor();
        roomSensor2.setSensor(sensor2);
        roomSensor2.setRoom(new Room());
        roomSensor2.getRoom().setId(2L);
        roomSensor2.getRoom().setName("Room2");
        // Mock RoomSensor 리스트 생성

        List<RoomSensor> mockRoomSensors = Arrays.asList(roomSensor1, roomSensor2);

        // Mock 동작 설정
        when(roomSensorRepository.findAll()).thenReturn(mockRoomSensors);

        // 메서드 호출
        List<SensorRoomMappingDto> result = predictedAirQualityService.getSensorMappingWithRoom();

        // 결과 검증
        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getSensorSerialNumber());
        assertEquals(LocalDateTime.of(2025, 3, 15, 10, 30), result.get(0).getSensorRegisterDate());
        assertEquals("67890", result.get(1).getSensorSerialNumber());
        assertEquals(LocalDateTime.of(2025, 3, 16, 14, 45), result.get(1).getSensorRegisterDate());
    }
    @Test
    void testSetPredictedAirQuality() {
        // given
        PredictedAirQualityDto dto = new PredictedAirQualityDto();
        dto.setSensorSerialNumber("1");
        dto.setTimestamp(LocalDateTime.parse("2023-10-01T12:00:00"));
        dto.setPm10(10.5f);
        dto.setCo2(400.0f);
        dto.setTvoc(0.5f);

        when(roomSensorRepository.findBySensor_SerialNumber("1"))
                .thenReturn(Optional.of(mockRoomSensor(1L)));

        when(predictedAirQualityRepository.findBySensorSerialNumberAndTimestamp("1", LocalDateTime.parse("2023-10-01T12:00:00")))
                .thenReturn(Optional.empty());

        // when
        predictedAirQualityService.setPredictedAirQuality(List.of(dto));

        // then
        verify(predictedAirQualityRepository, times(1)).save(any(PredictedAirQualityData.class));
    }

    @Test
    void testGetPredictedAirQuality() {
        // given
        String sensorSerialNumber = "1";
        when(predictedAirQualityRepository.findBySensorSerialNumberOrderByTimestamp(sensorSerialNumber))
                .thenReturn(List.of(mockPredictedAirQualityData()));

        // when
        List<PredictedAirQualityData> result = predictedAirQualityService.getPredictedAirQuality(sensorSerialNumber);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getSensorSerialNumber());
    }

    private PredictedAirQualityData mockPredictedAirQualityData() {
        return PredictedAirQualityData.builder()
                .id(1L)
                .sensorSerialNumber("1")
                .roomId(1L)
                .timestamp(LocalDateTime.now())
                .pm10(10.5f)
                .co2(400.0f)
                .tvoc(0.5f)
                .build();
    }

    private RoomSensor mockRoomSensor(Long roomId) {
        RoomSensor roomSensor = new RoomSensor();
        roomSensor.setRoom(mockRoom(roomId));
        return roomSensor;
    }

    private Room mockRoom(Long roomId) {
        Room room = new Room();
        room.setId(roomId);
        return room;
    }
}

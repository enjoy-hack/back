package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.predictedAirQualityDto.PredictedAirQualityDto;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
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
    void testSetPredictedAirQuality() {
        // given
        PredictedAirQualityDto dto = new PredictedAirQualityDto();
        dto.setSensorSerialNumber(1L);
        dto.setTimestamp("2023-10-01T12:00:00");
        dto.setPm10(10.5f);
        dto.setCo2(400.0f);
        dto.setTvoc(0.5f);

        when(roomSensorRepository.findBySensor_SerialNumber(1L))
                .thenReturn(Optional.of(mockRoomSensor(1L)));

        when(predictedAirQualityRepository.findBySensorSerialNumberAndTimestamp(1L, LocalDateTime.parse("2023-10-01T12:00:00")))
                .thenReturn(Optional.empty());

        // when
        predictedAirQualityService.setPredictedAirQuality(List.of(dto));

        // then
        verify(predictedAirQualityRepository, times(1)).save(any(PredictedAirQualityData.class));
    }

    @Test
    void testGetPredictedAirQuality() {
        // given
        Long sensorSerialNumber = 1L;
        when(predictedAirQualityRepository.findBySensorSerialNumberOrderByTimestamp(sensorSerialNumber))
                .thenReturn(List.of(mockPredictedAirQualityData()));

        // when
        List<PredictedAirQualityData> result = predictedAirQualityService.getPredictedAirQuality(sensorSerialNumber);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getSensorSerialNumber());
    }

    private PredictedAirQualityData mockPredictedAirQualityData() {
        return PredictedAirQualityData.builder()
                .id(1L)
                .sensorSerialNumber(1L)
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

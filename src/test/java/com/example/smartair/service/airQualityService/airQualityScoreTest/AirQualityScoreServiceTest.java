package com.example.smartair.service.airQualityService.airQualityScoreTest;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.PlaceAirQualityScore;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.PlaceAirQualityScoreRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.service.airQualityService.AirQualityScoreService;
import com.example.smartair.service.airQualityService.calculator.AirQualityCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
class AirQualityScoreServiceTest {

    @Mock
    private AirQualityCalculator airQualityCalculator;

    @Mock
    private AirQualityDataRepository airQualityDataRepository;

    @Mock
    private SensorAirQualityScoreRepository sensorAirQualityScoreRepository;

    @Mock
    private RoomAirQualityScoreRepository roomAirQualityScoreRepository;

    @Mock
    private RoomSensorRepository roomSensorRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AirQualityScoreService airQualityScoreService;

    private SensorAirQualityData testData;
    private Room testRoom;
    private Sensor testSensor;
    private SensorAirQualityScore calculatedDeviceScore;
    private RoomSensor testRoomSensor;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testRoom = Room.builder().id(1L).name("Test Room").build();
        testSensor = Sensor.builder().id(1L).build();
        testRoomSensor = RoomSensor.builder().id(1L).room(testRoom).sensor(testSensor).build();
        testData = SensorAirQualityData.builder().id(1L).sensor(testSensor).eco2(500).tvoc(300).build();

        when(roomSensorRepository.findBySensor(testSensor)).thenReturn(Optional.of(testRoomSensor));

        calculatedDeviceScore = new SensorAirQualityScore();
        calculatedDeviceScore.setId(100L);
        calculatedDeviceScore.setSensorAirQualityData(testData);
        calculatedDeviceScore.setOverallScore(50);
        calculatedDeviceScore.setPm10Score(30);
        calculatedDeviceScore.setPm25Score(20);
        calculatedDeviceScore.setEco2Score(50);
        calculatedDeviceScore.setTvocScore(40);
    }

    @Nested
    @DisplayName("공기질 점수 계산 시나리오")
    class CalculateScoreTests {

        @Test
        @DisplayName("주어진 SensorAirQualityData를 기반으로 공기질 점수 계산에 성공")
        void calculateAndSaveDeviceScore() {
            // given
            when(roomSensorRepository.findBySensor(testSensor)).thenReturn(Optional.of(testRoomSensor));
            when(airQualityCalculator.calculateScore(testData)).thenReturn(calculatedDeviceScore);
            when(sensorAirQualityScoreRepository.save(any(SensorAirQualityScore.class))).thenReturn(calculatedDeviceScore);

            // when
            airQualityScoreService.calculateAndSaveDeviceScore(testData);

            // then
            verify(sensorAirQualityScoreRepository).save(any(SensorAirQualityScore.class));

        }

    }
    }


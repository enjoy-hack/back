package com.example.smartair.service.airQualityService;

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
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.PlaceAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
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
    private SensorAirQualityScoreRepository deviceAirQualityScoreRepository;

    @Mock
    private RoomAirQualityScoreRepository roomAirQualityScoreRepository;

    @Mock
    private PlaceAirQualityScoreRepository placeAirQualityScoreRepository;

    @Mock
    private RoomSensorRepository roomSensorRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AirQualityScoreService airQualityScoreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("정상 처리 시나리오")
    class HappyPathTests {

        private SensorAirQualityData testData;
        private Room testRoom;
        private Place testPlace;
        private Sensor testSensor;
        private SensorAirQualityScore calculatedDeviceScore;
        private RoomSensor testRoomSensor;

        @BeforeEach
        void setupTestData() {
            testPlace = Place.builder().id(1L).name("Test Place").build();
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

        @Test
        @DisplayName("새로운 데이터 입력 시 모든 점수 기록 생성")
        void calculateAndSaveScores_NewData_ShouldCreateAllScores() {
            // Given
            when(airQualityCalculator.calculateScore(testData)).thenReturn(calculatedDeviceScore);
            when(deviceAirQualityScoreRepository.save(any(SensorAirQualityScore.class))).thenReturn(calculatedDeviceScore);

            // Room에 속한 Device 목록 Mocking 추가
            when(roomSensorRepository.findAllSensorByRoom(testRoom)).thenReturn(Collections.singletonList(testSensor));
            // testDevice에 대한 최신 점수 Mocking 추가
            when(deviceAirQualityScoreRepository.findFirstBySensorAirQualityData_SensorOrderByCreatedAtDesc(testSensor))
                    .thenReturn(Optional.of(calculatedDeviceScore));


            // 기존 RoomAirQualityScore 조회 Mocking (신규 생성을 테스트하는 경우라면 Optional.empty() 반환)
            when(roomAirQualityScoreRepository.findFirstByRoomOrderByCreatedAtDesc(testRoom)).thenReturn(Optional.empty());


            // RoomAirQualityScore 저장 Mocking
            RoomAirQualityScore savedRoomScore = new RoomAirQualityScore(); // 실제 저장될 객체처럼 mock
            savedRoomScore.setRoom(testRoom);
            // ... (점수 설정, ID 등 필요시 추가)
            when(roomAirQualityScoreRepository.save(any(RoomAirQualityScore.class))).thenReturn(savedRoomScore); // thenReturn(savedRoomScore) 또는 thenAnswer 사용


            // Place 점수 계산에 필요한 Room 점수 목록 Mocking
            // 이 테스트에서는 Place 점수도 검증하므로, testPlace에 속한 Room들의 점수 목록을 제공해야 합니다.
            // 여기서는 testRoom 하나만 있다고 가정하고, 위에서 저장된 savedRoomScore를 포함하는 리스트를 반환하도록 설정합니다.
//            when(roomRepository.findAllByPlace(testPlace)).thenReturn(Collections.singletonList(testRoom));
            when(roomAirQualityScoreRepository.findFirstByRoomOrderByCreatedAtDesc(testRoom)) // updatePlaceAverageScore 내부에서 호출됨
                    .thenReturn(Optional.of(savedRoomScore)); // 위에서 저장된 RoomScore를 반환하도록 설정

            // 기존 PlaceAirQualityScore 조회 Mocking (신규 생성을 테스트하는 경우라면 Optional.empty() 반환)
//            when(placeAirQualityScoreRepository.findFirstByPlaceOrderByCreatedAtDesc(testPlace)).thenReturn(Optional.empty());
            when(placeAirQualityScoreRepository.save(any(PlaceAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));


            // When
            airQualityScoreService.calculateAndSaveDeviceScore(testData);

            // Then
            verify(airQualityCalculator).calculateScore(eq(testData));
            verify(deviceAirQualityScoreRepository).save(any(SensorAirQualityScore.class)); // any() 또는 구체적인 객체
            verify(roomSensorRepository).findBySensor(eq(testSensor));
            verify(roomSensorRepository).findAllSensorByRoom(eq(testRoom)); // 호출 검증 추가
            verify(deviceAirQualityScoreRepository).findTopBySensorAirQualityData_SensorOrderByCreatedAtDesc(eq(testSensor)); // 호출 검증 추가


            ArgumentCaptor<RoomAirQualityScore> roomScoreCaptor = ArgumentCaptor.forClass(RoomAirQualityScore.class);
            verify(roomAirQualityScoreRepository).save(roomScoreCaptor.capture());
            RoomAirQualityScore capturedRoomScore = roomScoreCaptor.getValue();
            assertThat(capturedRoomScore.getRoom()).isEqualTo(testRoom);
            // ... (기존 roomScore 검증)

//            ArgumentCaptor<PlaceAirQualityScore> placeScoreCaptor = ArgumentCaptor.forClass(PlaceAirQualityScore.class);
            // verify(roomAirQualityScoreRepository).findByRoom_Place(eq(testPlace)); // 이 부분은 서비스 로직 변경으로 불필요
//            verify(roomRepository).findAllByPlace(eq(testPlace)); // 호출 검증 추가
//            verify(placeAirQualityScoreRepository).save(placeScoreCaptor.capture());
//            PlaceAirQualityScore capturedPlaceScore = placeScoreCaptor.getValue();
//            assertThat(capturedPlaceScore.getPlace()).isEqualTo(testPlace);
            // ... (기존 placeScore 검증, 평균 계산 로직에 따라 기대값 조정 필요)
        }

        @Nested
        @DisplayName("예외 처리 시나리오")
        class ExceptionTests {

            @Test
            @DisplayName("AirQualityData가 null일 때 CustomException(INVALID_INPUT_DATA) 발생")
            void calculateAndSaveScores_NullData_ShouldThrowException() {
                // Given
                SensorAirQualityData nullData = null;

                // When & Then
                CustomException exception = assertThrows(CustomException.class, () -> {
                    airQualityScoreService.calculateAndSaveDeviceScore(nullData);
                });
                assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_DATA);
                verifyNoInteractions(airQualityCalculator, deviceAirQualityScoreRepository, roomAirQualityScoreRepository, placeAirQualityScoreRepository);
            }

            @Test
            @DisplayName("AirQualityData의 Device가 null이거나 Room-Device 매핑이 없을 때 CustomException 발생")
            void calculateAndSaveScores_NullDeviceOrMappingNotFound_ShouldThrowException() {
                // Given
                // Case 1: Device is null
                SensorAirQualityData dataWithNullDevice = SensorAirQualityData.builder().id(1L).sensor(null).build();

                // Case 2: Room-Device Mapping Not Found
                Sensor testSensor = Sensor.builder().id(1L).build(); // Room 정보 없는 Device
                SensorAirQualityData dataWithNoMapping = SensorAirQualityData.builder().id(1L).sensor(testSensor).build();
                when(roomSensorRepository.findBySensor(testSensor)).thenReturn(Optional.empty()); // 매핑 없음 Mocking

                // When & Then for Case 1 (DEVICE_NOT_FOUND)
                CustomException exception1 = assertThrows(CustomException.class, () -> {
                    airQualityScoreService.calculateAndSaveDeviceScore(dataWithNullDevice);
                });
                assertEquals(ErrorCode.DEVICE_NOT_FOUND, exception1.getErrorCode());

                // When & Then for Case 2 (ROOM_DEVICE_MAPPING_NOT_FOUND)
                CustomException exception2 = assertThrows(CustomException.class, () -> {
                    airQualityScoreService.calculateAndSaveDeviceScore(dataWithNoMapping);
                });
                assertEquals(ErrorCode.ROOM_DEVICE_MAPPING_NOT_FOUND, exception2.getErrorCode());

                verifyNoInteractions(airQualityCalculator, deviceAirQualityScoreRepository, roomAirQualityScoreRepository, placeAirQualityScoreRepository);
            }


//            @Test
//            @DisplayName("Room의 Place가 null일 때 Place 점수 생성 안 함")
//            void calculateAndSaveScores_NullPlace_ShouldLogWarningAndSkipPlaceScore() {
//                // Given
////                Room roomWithNullPlace = Room.builder().id(1L).name("RoomWithNullPlace").place(null).build(); // name 추가 (로깅용)
//                Sensor sensorInRoomWithNullPlace = Sensor.builder().id(1L).build();
////                RoomSensor roomSensorMapping = RoomSensor.builder().id(1L).room(roomWithNullPlace).sensor(sensorInRoomWithNullPlace).build();
//                SensorAirQualityData dataWithNullPlace = SensorAirQualityData.builder().id(1L).sensor(sensorInRoomWithNullPlace).build();
//                SensorAirQualityScore mockDeviceScore = new SensorAirQualityScore(); // Device 점수는 생성되어야 함
//                mockDeviceScore.setOverallScore(70); // 예시 점수
//
//                // Mocking 설정
//                when(roomSensorRepository.findBySensor(sensorInRoomWithNullPlace)).thenReturn(Optional.of(roomSensorMapping));
//                when(airQualityCalculator.calculateScore(dataWithNullPlace)).thenReturn(mockDeviceScore);
//                when(deviceAirQualityScoreRepository.save(any(SensorAirQualityScore.class))).thenReturn(mockDeviceScore);
//
//                // Room에 속한 Device 목록 Mocking 추가
//                when(roomSensorRepository.findAllSensorByRoom(roomWithNullPlace)).thenReturn(Collections.singletonList(sensorInRoomWithNullPlace));
//                // deviceInRoomWithNullPlace에 대한 최신 점수 Mocking 추가
//                when(deviceAirQualityScoreRepository.findTopBySensorAirQualityData_SensorOrderByCreatedAtDesc(sensorInRoomWithNullPlace))
//                        .thenReturn(Optional.of(mockDeviceScore));
//
//                // 기존 RoomAirQualityScore 조회 Mocking (신규 생성)
//                when(roomAirQualityScoreRepository.findFirstByRoomOrderByCreatedAtDesc(roomWithNullPlace)).thenReturn(Optional.empty());
//                // RoomAirQualityScore 저장 Mocking
//                when(roomAirQualityScoreRepository.save(any(RoomAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));
//
//
//                // When
//                airQualityScoreService.calculateAndSaveDeviceScore(dataWithNullPlace);
//
//                // Then
//                verify(airQualityCalculator).calculateScore(eq(dataWithNullPlace));
//                verify(deviceAirQualityScoreRepository).save(any(SensorAirQualityScore.class));
//                verify(roomSensorRepository).findBySensor(eq(sensorInRoomWithNullPlace));
//                verify(roomSensorRepository).findAllSensorByRoom(eq(roomWithNullPlace)); // 호출 검증
//                verify(deviceAirQualityScoreRepository).findTopBySensorAirQualityData_SensorOrderByCreatedAtDesc(eq(sensorInRoomWithNullPlace)); // 호출 검증
//
//                // RoomAirQualityScore는 저장되어야 함
//                verify(roomAirQualityScoreRepository).save(any(RoomAirQualityScore.class));
//                // PlaceAirQualityScore는 저장되지 않아야 함
//                verify(placeAirQualityScoreRepository, never()).save(any(PlaceAirQualityScore.class));
//            }
        }
    }
    }


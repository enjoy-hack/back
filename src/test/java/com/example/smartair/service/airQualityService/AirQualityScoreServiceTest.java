package com.example.smartair.service.airQualityService;

import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.entity.airScore.DeviceAirQualityScore;
import com.example.smartair.entity.airScore.RoomAirQualityScore;
import com.example.smartair.entity.airScore.PlaceAirQualityScore;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityScoreRepository.DeviceAirQualityScoreRepository;
import com.example.smartair.repository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.airQualityScoreRepository.PlaceAirQualityScoreRepository;
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
    private DeviceAirQualityScoreRepository deviceAirQualityScoreRepository;

    @Mock
    private RoomAirQualityScoreRepository roomAirQualityScoreRepository;

    @Mock
    private PlaceAirQualityScoreRepository placeAirQualityScoreRepository;

    @InjectMocks
    private AirQualityScoreService airQualityScoreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("정상 처리 시나리오")
    class HappyPathTests {

        private AirQualityData testData;
        private Room testRoom;
        private Place testPlace;
        private DeviceAirQualityScore calculatedDeviceScore;

        @BeforeEach
        void setupTestData() {
            testPlace = Place.builder().id(1L).name("Test Place").build();
            testRoom = Room.builder().id(1L).name("Test Room").place(testPlace).build();
            testData = AirQualityData.builder().id(1L).room(testRoom).eco2(500).tvoc(300).build();

            calculatedDeviceScore = new DeviceAirQualityScore();
            calculatedDeviceScore.setId(100L);
            calculatedDeviceScore.setAirQualityData(testData);
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
            when(deviceAirQualityScoreRepository.save(any(DeviceAirQualityScore.class))).thenReturn(calculatedDeviceScore);

            // createPlaceAirQualityScore 내부에서 findByPlace 호출 시뮬레이션
            RoomAirQualityScore roomScoreForAvg = new RoomAirQualityScore(); // 평균 계산에 사용될 객체 가정
            roomScoreForAvg.setOverallScore(calculatedDeviceScore.getOverallScore());
            // ... 다른 점수들도 설정 ...
            when(roomAirQualityScoreRepository.findByPlace(testPlace)).thenReturn(Collections.singletonList(roomScoreForAvg));
            when(roomAirQualityScoreRepository.save(any(RoomAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));
            when(placeAirQualityScoreRepository.save(any(PlaceAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            airQualityScoreService.calculateAndSaveScores(testData);

            // Then
            verify(airQualityCalculator).calculateScore(eq(testData));
            verify(deviceAirQualityScoreRepository).save(eq(calculatedDeviceScore));

            // RoomAirQualityScore 생성 검증
            ArgumentCaptor<RoomAirQualityScore> roomScoreCaptor = ArgumentCaptor.forClass(RoomAirQualityScore.class);
            verify(roomAirQualityScoreRepository).save(roomScoreCaptor.capture());
            RoomAirQualityScore capturedRoomScore = roomScoreCaptor.getValue();
            assertThat(capturedRoomScore.getRoom()).isEqualTo(testRoom);
            assertThat(capturedRoomScore.getPlace()).isEqualTo(testPlace);
            assertThat(capturedRoomScore.getOverallScore()).isEqualTo(calculatedDeviceScore.getOverallScore());
            assertThat(capturedRoomScore.getPm10Score()).isEqualTo(calculatedDeviceScore.getPm10Score());

            // PlaceAirQualityScore 생성 검증 (Total -> Place)
             ArgumentCaptor<PlaceAirQualityScore> placeScoreCaptor = ArgumentCaptor.forClass(PlaceAirQualityScore.class);
            verify(roomAirQualityScoreRepository).findByPlace(eq(testPlace));
            verify(placeAirQualityScoreRepository).save(placeScoreCaptor.capture());
            PlaceAirQualityScore capturedPlaceScore = placeScoreCaptor.getValue();
            assertThat(capturedPlaceScore.getPlace()).isEqualTo(testPlace);
            assertThat(capturedPlaceScore.getOverallScore()).isEqualTo(roomScoreForAvg.getOverallScore());
        }
    }

    @Nested
    @DisplayName("예외 처리 시나리오")
    class ExceptionTests {

        @Test
        @DisplayName("AirQualityData가 null일 때 CustomException(INVALID_INPUT_DATA) 발생")
        void calculateAndSaveScores_NullData_ShouldThrowException() {
            // Given
            AirQualityData nullData = null;

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> {
                airQualityScoreService.calculateAndSaveScores(nullData);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_DATA);
            verifyNoInteractions(airQualityCalculator, deviceAirQualityScoreRepository, roomAirQualityScoreRepository, placeAirQualityScoreRepository);
        }

        @Test
        @DisplayName("AirQualityData의 Room이 null일 때 CustomException(ROOM_NOT_FOUND) 발생")
        void calculateAndSaveScores_NullRoom_ShouldThrowException() {
            // Given
            AirQualityData dataWithNullRoom = AirQualityData.builder().id(1L).room(null).build();

            // When & Then
            CustomException exception = assertThrows(CustomException.class, () -> {
                airQualityScoreService.calculateAndSaveScores(dataWithNullRoom);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROOM_NOT_FOUND);
            verifyNoInteractions(airQualityCalculator, deviceAirQualityScoreRepository, roomAirQualityScoreRepository, placeAirQualityScoreRepository);
        }

         @Test
        @DisplayName("Room의 Place가 null일 때 Place 점수 생성 안 함")
        void calculateAndSaveScores_NullPlace_ShouldLogWarningAndSkipPlaceScore() {
            // Given
            Room roomWithNullPlace = Room.builder().id(1L).place(null).build();
            AirQualityData dataWithNullPlace = AirQualityData.builder().id(1L).room(roomWithNullPlace).build();
            DeviceAirQualityScore mockDeviceScore = new DeviceAirQualityScore();

            when(airQualityCalculator.calculateScore(dataWithNullPlace)).thenReturn(mockDeviceScore);
            when(deviceAirQualityScoreRepository.save(any(DeviceAirQualityScore.class))).thenReturn(mockDeviceScore);
            when(roomAirQualityScoreRepository.save(any(RoomAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            airQualityScoreService.calculateAndSaveScores(dataWithNullPlace);

            // Then
            verify(airQualityCalculator).calculateScore(eq(dataWithNullPlace));
            verify(deviceAirQualityScoreRepository).save(any(DeviceAirQualityScore.class));
            verify(roomAirQualityScoreRepository).save(any(RoomAirQualityScore.class));
            verify(placeAirQualityScoreRepository, never()).save(any(PlaceAirQualityScore.class));
        }
    }
}
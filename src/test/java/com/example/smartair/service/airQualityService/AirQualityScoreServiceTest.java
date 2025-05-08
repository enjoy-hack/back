package com.example.smartair.service.airQualityService;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.DeviceAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.PlaceAirQualityScore;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityScoreRepository.DeviceAirQualityScoreRepository;
import com.example.smartair.repository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.airQualityScoreRepository.PlaceAirQualityScoreRepository;
import com.example.smartair.repository.roomDeviceRepository.RoomDeviceRepository;
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

    @Mock
    private RoomDeviceRepository roomDeviceRepository;

    @InjectMocks
    private AirQualityScoreService airQualityScoreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    @DisplayName("정상 처리 시나리오")
    class HappyPathTests {

        private DeviceAirQualityData testData;
        private Room testRoom;
        private Place testPlace;
        private Device testDevice;
        private DeviceAirQualityScore calculatedDeviceScore;
        private RoomDevice testRoomDevice;

        @BeforeEach
        void setupTestData() {
            testPlace = Place.builder().id(1L).name("Test Place").build();
            testRoom = Room.builder().id(1L).name("Test Room").place(testPlace).build();
            testDevice = Device.builder().id(1L).build();
            testRoomDevice = RoomDevice.builder().id(1L).room(testRoom).device(testDevice).build();
            testData = DeviceAirQualityData.builder().id(1L).device(testDevice).eco2(500).tvoc(300).build();

            when(roomDeviceRepository.findByDevice(testDevice)).thenReturn(Optional.of(testRoomDevice));

            calculatedDeviceScore = new DeviceAirQualityScore();
            calculatedDeviceScore.setId(100L);
            calculatedDeviceScore.setDeviceAirQualityData(testData);
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

            RoomAirQualityScore roomScoreForAvg = new RoomAirQualityScore();
            roomScoreForAvg.setOverallScore(calculatedDeviceScore.getOverallScore());
            when(roomAirQualityScoreRepository.findByRoom_Place(testPlace)).thenReturn(Collections.singletonList(roomScoreForAvg));
            when(roomAirQualityScoreRepository.save(any(RoomAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));
            when(placeAirQualityScoreRepository.save(any(PlaceAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            airQualityScoreService.calculateAndSaveDeviceScore(testData);

            // Then
            verify(airQualityCalculator).calculateScore(eq(testData));
            verify(deviceAirQualityScoreRepository).save(eq(calculatedDeviceScore));
            verify(roomDeviceRepository).findByDevice(eq(testDevice));

            ArgumentCaptor<RoomAirQualityScore> roomScoreCaptor = ArgumentCaptor.forClass(RoomAirQualityScore.class);
            verify(roomAirQualityScoreRepository).save(roomScoreCaptor.capture());
            RoomAirQualityScore capturedRoomScore = roomScoreCaptor.getValue();
            assertThat(capturedRoomScore.getRoom()).isEqualTo(testRoom);
            assertThat(capturedRoomScore.getRoom().getPlace()).isEqualTo(testPlace);
            assertThat(capturedRoomScore.getOverallScore()).isEqualTo(calculatedDeviceScore.getOverallScore());
            assertThat(capturedRoomScore.getPm10Score()).isEqualTo(calculatedDeviceScore.getPm10Score());

            ArgumentCaptor<PlaceAirQualityScore> placeScoreCaptor = ArgumentCaptor.forClass(PlaceAirQualityScore.class);
            verify(roomAirQualityScoreRepository).findByRoom_Place(eq(testPlace));
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
            DeviceAirQualityData nullData = null;

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
            DeviceAirQualityData dataWithNullDevice = DeviceAirQualityData.builder().id(1L).device(null).build();

            // Case 2: Room-Device Mapping Not Found
            Device testDevice = Device.builder().id(1L).build(); // Room 정보 없는 Device
            DeviceAirQualityData dataWithNoMapping = DeviceAirQualityData.builder().id(1L).device(testDevice).build();
            when(roomDeviceRepository.findByDevice(testDevice)).thenReturn(Optional.empty()); // 매핑 없음 Mocking

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

         @Test
        @DisplayName("Room의 Place가 null일 때 Place 점수 생성 안 함")
        void calculateAndSaveScores_NullPlace_ShouldLogWarningAndSkipPlaceScore() {
            // Given
            Room roomWithNullPlace = Room.builder().id(1L).place(null).build();
            Device deviceInRoomWithNullPlace = Device.builder().id(1L).build(); // Room 정보 없음
            RoomDevice roomDeviceMapping = RoomDevice.builder().room(roomWithNullPlace).device(deviceInRoomWithNullPlace).build();
            DeviceAirQualityData dataWithNullPlace = DeviceAirQualityData.builder().id(1L).device(deviceInRoomWithNullPlace).build();
            DeviceAirQualityScore mockDeviceScore = new DeviceAirQualityScore();

            // Mocking 설정
            when(roomDeviceRepository.findByDevice(deviceInRoomWithNullPlace)).thenReturn(Optional.of(roomDeviceMapping)); // RoomDevice 반환
            when(airQualityCalculator.calculateScore(dataWithNullPlace)).thenReturn(mockDeviceScore);
            when(deviceAirQualityScoreRepository.save(any(DeviceAirQualityScore.class))).thenReturn(mockDeviceScore);
            when(roomAirQualityScoreRepository.save(any(RoomAirQualityScore.class))).thenAnswer(inv -> inv.getArgument(0));
            // Place 점수 계산에 필요한 room 점수 조회 로직 mocking 필요
            when(roomAirQualityScoreRepository.findByRoom_Place(null)).thenReturn(Collections.emptyList()); // findByRoom_Place(null) 호출 예상

            // When
            airQualityScoreService.calculateAndSaveDeviceScore(dataWithNullPlace);

            // Then
            verify(airQualityCalculator).calculateScore(eq(dataWithNullPlace));
            verify(deviceAirQualityScoreRepository).save(any(DeviceAirQualityScore.class));
            verify(roomDeviceRepository).findByDevice(eq(deviceInRoomWithNullPlace)); // RoomDeviceRepository 호출 검증
            verify(roomAirQualityScoreRepository).save(any(RoomAirQualityScore.class));
            verify(placeAirQualityScoreRepository, never()).save(any(PlaceAirQualityScore.class));
        }
    }
}
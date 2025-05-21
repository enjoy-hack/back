package com.example.smartair.service.airQualityService;
import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.exception.CustomException;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.AnomalyReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlyDeviceAirQualitySnapshotRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.service.airQualityService.report.AnomalyReportService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnomalyReportServiceTest {

    @InjectMocks
    private AnomalyReportService anomalyReportService;

    @Mock
    private AnomalyReportRepository anomalyReportRepository;

    @Mock
    private SensorRepository sensorRepository;

    @Mock
    private HourlyDeviceAirQualitySnapshotRepository hourlyDeviceAirQualitySnapshotRepository;

    @Mock
    private DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        anomalyReportService = new AnomalyReportService(
                anomalyReportRepository,
                sensorRepository,
                hourlyDeviceAirQualitySnapshotRepository,
                dailySensorAirQualityReportRepository
        );
    }

    @Test
    void setAnomalyReport_success() throws FirebaseMessagingException {
        // Given
        String timestamp = "2025-05-21 15:00:00";
        AnomalyReportDto dto = AnomalyReportDto.builder()
                .sensorSerialNumber("ABC123")
                .pollutant("CO2")
                .pollutantValue(800.0)
                .predictedValue(600.0)
                .anomalyTimestamp(timestamp)
                .build();

        Sensor sensor = Sensor.builder().serialNumber("ABC123").build();
        sensor.setUser(new com.example.smartair.entity.user.User()); // 유저 객체가 있어야 FCM 토큰 존재
        sensor.getUser().setFcmToken("fake_fcm_token");

        LocalDateTime anomalyDateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        when(sensorRepository.findBySerialNumber("ABC123")).thenReturn(Optional.of(sensor));
        when(hourlyDeviceAirQualitySnapshotRepository.findBySensorAndSnapshotHour(sensor, anomalyDateTime))
                .thenReturn(Optional.of(mock(HourlySensorAirQualitySnapshot.class)));
        when(dailySensorAirQualityReportRepository.findBySensorAndReportDate(sensor, anomalyDateTime.toLocalDate()))
                .thenReturn(Optional.of(mock(DailySensorAirQualityReport.class)));
        when(anomalyReportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Firebase 메시징 목업
        mockStatic(FirebaseMessaging.class);
        FirebaseMessaging mockMessaging = mock(FirebaseMessaging.class);
        when(FirebaseMessaging.getInstance()).thenReturn(mockMessaging);
        when(mockMessaging.send(any(Message.class))).thenReturn("message_id_123");

        // When
        String result = anomalyReportService.setAnomalyReport(dto);

        // Then
        assertEquals("message_id_123", result);
        verify(anomalyReportRepository, times(1)).save(any());
    }

    @Test
    void setAnomalyReport_sensorNotFound() {
        // Given
        AnomalyReportDto dto = AnomalyReportDto.builder()
                .sensorSerialNumber("NOT_EXIST")
                .pollutant("CO2")
                .pollutantValue(800.0)
                .predictedValue(600.0)
                .anomalyTimestamp("2025-05-21 15:00:00")
                .build();

        when(sensorRepository.findBySerialNumber("NOT_EXIST")).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomException.class, () -> anomalyReportService.setAnomalyReport(dto));
    }

    @Test
    void generateDescription_highErrorRate_returnsWarning() {
        // Given
        String pollutant = "CO2";
        double actual = 1000;
        double predicted = 500;
        String timestamp = "2025-05-21 15:00:00";

        // When
        String description = anomalyReportService.generateDescription(pollutant, actual, predicted, timestamp);

        // Then
        assertTrue(description.contains("즉각적인 점검이 필요할 수 있습니다."));
    }

    @Test
    void getAnomalyReports_validDatesAndSensor_returnsList() {
        // Given
        String serialNumber = "ABC123";
        Sensor sensor = Sensor.builder().serialNumber(serialNumber).build();
        LocalDate startDate = LocalDate.of(2025, 5, 20);
        LocalDate endDate = LocalDate.of(2025, 5, 21);

        when(sensorRepository.findBySerialNumber(serialNumber)).thenReturn(Optional.of(sensor));
        when(anomalyReportRepository.findAnomaliesBySensorAndDateRange(any(), any(), any()))
                .thenReturn(List.of(mock(AnomalyReport.class)));

        // When
        var results = anomalyReportService.getAnomalyReports(serialNumber, startDate, endDate);

        // Then
        assertEquals(1, results.size());
    }
}

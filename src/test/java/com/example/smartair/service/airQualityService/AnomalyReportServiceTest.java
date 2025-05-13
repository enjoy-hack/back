package com.example.smartair.service.airQualityService;

import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.AnomalyReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlyDeviceAirQualitySnapshotRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.service.airQualityService.report.AnomalyReportService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AnomalyReportServiceTest {

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

    @InjectMocks
    private AnomalyReportService anomalyReportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetAnomalyReport_Success() throws FirebaseMessagingException {
        // given
        AnomalyReportDto dto = new AnomalyReportDto();
        dto.setSensorSerialNumber(12345L);
        dto.setAnomalyTimestamp("2023-10-10 10:00:00");
        dto.setPollutant("PM10");
        dto.setPollutantValue(150.0);
        dto.setPredictedValue(100.0);

        Sensor sensor = new Sensor();
        sensor.setSerialNumber(12345L);
        User user = mock(User.class);
        sensor.setUser(user);
        when(user.getFcmToken()).thenReturn("mockToken");

        HourlySensorAirQualitySnapshot hourlySnapshot = new HourlySensorAirQualitySnapshot();
        DailySensorAirQualityReport dailyReport = new DailySensorAirQualityReport();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime anomalyTime = LocalDateTime.parse(dto.getAnomalyTimestamp(), formatter);
        LocalDate anomalyDate = anomalyTime.toLocalDate();

        when(sensorRepository.findBySerialNumber(12345L)).thenReturn(Optional.of(sensor));
        when(hourlyDeviceAirQualitySnapshotRepository.findBySensorAndSnapshotHour(sensor, anomalyTime))
                .thenReturn(Optional.of(hourlySnapshot));
        when(dailySensorAirQualityReportRepository.findBySensorAndReportDate(sensor, anomalyDate))
                .thenReturn(Optional.of(dailyReport));

        // ✅ FirebaseMessaging을 모킹: static call 우회
        mockStatic(FirebaseMessaging.class).when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
        when(firebaseMessaging.send(any())).thenReturn("messageId");

        // when
        String result = anomalyReportService.setAnomalyReport(dto);

        // then
        assertEquals("messageId", result);
        verify(anomalyReportRepository, times(1)).save(any(AnomalyReport.class));
    }



    @Test
    void testGetAnomalyReports_Success() {
        // given
        Long sensorSerialNumber = 12345L;
        LocalDate startDate = LocalDate.of(2023, 10, 1);
        LocalDate endDate = LocalDate.of(2023, 10, 31);

        Sensor sensor = new Sensor();
        sensor.setSerialNumber(Long.valueOf("12345"));

        AnomalyReport report1 = new AnomalyReport();
        AnomalyReport report2 = new AnomalyReport();

        when(sensorRepository.findBySerialNumber(sensorSerialNumber)).thenReturn(Optional.of(sensor));
        when(anomalyReportRepository.findOverlappingAnomalyReports(sensor, startDate, endDate))
                .thenReturn(List.of(report1, report2));

        // when
        List<AnomalyReport> result = anomalyReportService.getAnomalyReports(sensorSerialNumber, startDate, endDate);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(anomalyReportRepository, times(1)).findOverlappingAnomalyReports(sensor, startDate, endDate);
    }
}
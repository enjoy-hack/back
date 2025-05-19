package com.example.smartair.service.airQualityService.report;

import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.AnomalyReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlyDeviceAirQualitySnapshotRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.google.firebase.messaging.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class AnomalyReportService {
    private final AnomalyReportRepository anomalyReportRepository;
    private final SensorRepository sensorRepository;
    private final HourlyDeviceAirQualitySnapshotRepository hourlyDeviceAirQualitySnapshotRepository;
    private final DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;

    private static final DateTimeFormatter ANOMALY_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String setAnomalyReport(AnomalyReportDto dto) {
        Sensor sensor = sensorRepository.findBySerialNumber(dto.getSensorSerialNumber())
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found with serial number: " + dto.getSensorSerialNumber()));

        LocalDateTime anomalyDate = LocalDateTime.parse(dto.getAnomalyTimestamp(), ANOMALY_TIMESTAMP_FORMATTER);


        HourlySensorAirQualitySnapshot hourlySnapshot = hourlyDeviceAirQualitySnapshotRepository
                .findBySensorAndSnapshotHour(sensor, anomalyDate)
                .orElseThrow(() -> new IllegalArgumentException("Hourly snapshot not found for sensor: " + sensor.getSerialNumber() + " at date: " + anomalyDate));

        DailySensorAirQualityReport dailyReport = dailySensorAirQualityReportRepository
                .findBySensorAndReportDate(sensor, LocalDate.from(anomalyDate))
                .orElseThrow(() -> new IllegalArgumentException("Daily report not found for sensor: " + sensor.getSerialNumber() + " at date: " + LocalDate.from(anomalyDate)));

        String description = generateDescription(dto.getPollutant(), dto.getPollutantValue(), dto.getPredictedValue()); // 예측값과 실제값 비교하여 설명 생성

        AnomalyReport anomalyReport = AnomalyReport.builder()
                .sensor(sensor)
                .anomalyTimestamp(LocalDateTime.parse(dto.getAnomalyTimestamp(), ANOMALY_TIMESTAMP_FORMATTER))
                .pollutant(Pollutant.valueOf(dto.getPollutant()))
                .pollutantValue(dto.getPollutantValue())
                .description(description)
                .relatedHourlySnapshot(hourlySnapshot)
                .relatedDailyReport(dailyReport)
                .build();

        anomalyReportRepository.save(anomalyReport);
        log.info("Anomaly report saved: {}", anomalyReport);

        String targetToken = sensor.getUser().getFcmToken(); // 알림 대상 FCM 토큰 가져오기

        Message message = Message.builder()
                .setToken(targetToken)
                .putData("title", "이상치 발생 알림")
                .putData("body", String.format("%s 농도가 %.2f로 예측치 %.2f와 비교했을 때 %s",
                        dto.getPollutant(), dto.getPollutantValue(), dto.getPredictedValue(), description))
                .build();
        try {
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode().equals(MessagingErrorCode.INVALID_ARGUMENT)) {
                // 토큰이 유효하지 않은 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else if (e.getMessagingErrorCode().equals(MessagingErrorCode.UNREGISTERED)) {
                // 재발급된 이전 토큰인 경우, 오류 코드를 반환
                return e.getMessagingErrorCode().toString();
            } else { // 그 외, 오류는 런타임 예외로 처리
                throw new RuntimeException("Error sending FCM message: " + e.getMessage());
            }
        }

    }

    public String generateDescription(String pollutant, double actual, double predicted) {
        double errorRate = Math.abs(actual - predicted) / (predicted == 0 ? 1 : predicted); // 0 나눔 방지
        String level;

        if (errorRate >= 0.3) {
            level = "예측값보다 상당히 높은 수치입니다. 즉각적인 점검이 필요할 수 있습니다.";
        } else if (errorRate >= 0.1) {
            level = "예측값과 다소 차이가 있는 수치입니다. 주의가 요구됩니다.";
        } else {
            level = "예측값과 유사하여 정상 범위로 판단됩니다.";
        }

        return String.format(
                "%s 농도가 %.2f로 예측치 %.2f와 비교했을 때 %s",
                pollutant, actual, predicted, level
        );
    }

    public List<AnomalyReport> getAnomalyReports(Long sensorSerialNumber, LocalDate startDate, LocalDate endDate) {
        Sensor sensor = sensorRepository.findBySerialNumber(sensorSerialNumber)
                .orElseThrow(() -> new IllegalArgumentException("Sensor not found with serial number: " + sensorSerialNumber));

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        return anomalyReportRepository.findAnomaliesBySensorAndDateRange(sensor,startDateTime, endDateTime);
    }


}

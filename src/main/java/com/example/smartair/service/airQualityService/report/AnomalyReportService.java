package com.example.smartair.service.airQualityService.report;

import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
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
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, String.format("시리얼 번호 {}에 맞는 센서가 존재하지 않습니다." + dto.getSensorSerialNumber())));

        LocalDateTime anomalyDate = LocalDateTime.parse(dto.getAnomalyTimestamp(), ANOMALY_TIMESTAMP_FORMATTER);


        HourlySensorAirQualitySnapshot hourlySnapshot = hourlyDeviceAirQualitySnapshotRepository
                .findBySensorAndSnapshotHour(sensor, anomalyDate)
                .orElseThrow(() -> new CustomException(ErrorCode.SNAPSHOT_NOT_FOUND, String.format("해당 센서의 시간별 스냅샷을 찾을 수 없습니다. 시리얼 번호: %s, 날짜: %s", sensor.getSerialNumber(), anomalyDate)));

        DailySensorAirQualityReport dailyReport = dailySensorAirQualityReportRepository
                .findBySensorAndReportDate(sensor, LocalDate.from(anomalyDate))
                .orElseThrow(() -> new CustomException(ErrorCode.NO_DAILY_REPORTS_FOUND, String.format("해당 센서의 일일 보고서를 찾을 수 없습니다. 시리얼 번호: %s, 날짜: %s", sensor.getSerialNumber(), LocalDate.from(anomalyDate))));

        String description = generateDescription(
                dto.getPollutant(),
                dto.getPollutantValue(),
                dto.getPredictedValue(),
                dto.getAnomalyTimestamp() // 추가된 인자
        );
        log.info("Anomaly report description: {}", description);

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
        log.info("Anomaly report saved: {}", anomalyReport.getId());

        String targetToken = sensor.getUser().getFcmToken(); // 알림 대상 FCM 토큰 가져오기

        Message message = Message.builder()
                .setToken(targetToken)
                .putData("title", "이상치 발생 알림")
                .putData("body", description)
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
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "Error sending FCM message: " + e.getMessage());
            }
        }

    }

    public String generateDescription(String pollutant, double actual, double predicted, String anomalyTimestamp) {

        double errorRate = Math.abs(actual - predicted) / (predicted == 0 ? 1 : predicted); // 오차율 계산, 예측값이 0일 경우 1로 나누기

        // 오염물질별 기본 임계값
        double baseThreshold;
        switch (pollutant.toUpperCase()) {
            case "CO2":
                baseThreshold = 0.10;
                break;
            case "TVOC":
            case "PM10":
                baseThreshold = 0.20;
                break;
            default:
                baseThreshold = 0.25; // 기타 오염물질
        }

        // 시간대별 가중치 설정
        int hour = LocalDateTime.parse(anomalyTimestamp, ANOMALY_TIMESTAMP_FORMATTER).getHour();
        double timeFactor;
        if (hour < 6) {
            timeFactor = 0.8;  // 새벽
        } else if (hour < 12) {
            timeFactor = 1.0;  // 오전
        } else if (hour < 18) {
            timeFactor = 1.1;  // 오후
        } else {
            timeFactor = 0.9;  // 저녁
        }

        // 최종 임계값 계산
        double threshold = baseThreshold * timeFactor;

        String level;
        if (errorRate >= threshold + 0.1) {
            level = "예측값보다 상당히 높은 수치입니다. 즉각적인 점검이 필요할 수 있습니다.";
        } else if (errorRate >= threshold) {
            level = "예측값과 다소 차이가 있는 수치입니다. 주의가 요구됩니다.";
        } else {
            level = "예측값과 유사하여 정상 범위로 판단됩니다.";
        }

        return String.format("%s 농도가 %.2f로 예측치 %.2f와 비교했을 때, %.0f%% 정도로 %s",
                pollutant, actual, predicted, errorRate*100,level);
    }

    public List<AnomalyReport> getAnomalyReports(String serialNumber, LocalDate startDate, LocalDate endDate) {
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND,String.format("해당 센서를 찾을 수 없습니다. serialNumber: %d", serialNumber)));

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        return anomalyReportRepository.findAnomaliesBySensorAndDateRange(sensor,startDateTime, endDateTime);
    }


}

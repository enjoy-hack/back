package com.example.smartair.service.airQualityService.report;

import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportResponseDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.notification.Notification;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomParticipant.RoomParticipant;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.AnomalyReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlyDeviceAirQualitySnapshotRepository;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.notificationRepository.NotificationRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.service.deviceService.ThinQService;
import com.google.firebase.messaging.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class AnomalyReportService {
    private final AnomalyReportRepository anomalyReportRepository;
    private final SensorRepository sensorRepository;
    private final HourlyDeviceAirQualitySnapshotRepository hourlyDeviceAirQualitySnapshotRepository;
    private final DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;
    private final ThinQService thinQService;
    private final RoomSensorRepository roomSensorRepository;
    private final DeviceRepository deviceRepository;
    private final NotificationRepository notificationRepository;


    private static final DateTimeFormatter ANOMALY_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String setAnomalyReport(AnomalyReportDto dto) throws Exception {
        Sensor sensor = sensorRepository.findBySerialNumber(dto.getSensorSerialNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, String.format("시리얼 번호 %s에 맞는 센서가 존재하지 않습니다.", dto.getSensorSerialNumber())));

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
                dto.getAnomalyTimestamp()
        );
        log.info("Anomaly report description: {}", description);

        // 이상치 보고서 생성
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

        Optional<RoomSensor> roomSensor = roomSensorRepository.findBySensor(sensor);
        if(roomSensor.isEmpty()){ // 방에 등록되지 않은 센서인 경우, 센서 주인에게만 알림만
            return firebaseAlarm(sensor.getUser().getFcmToken(), description, sensor.getUser());
        }else{
            Room room = roomSensor.get().getRoom();

            List<User> users = new ArrayList<>(); // 방 참여자들에게 알림을 보내기 위한 리스트
            users.add(room.getOwner()); // 방 주인
            for (RoomParticipant participant: room.getParticipants()) { // 방 참여자들에게 알림
                users.add(participant.getUser());
            }
            // 알림 전송
            for (User user : users) {
                firebaseAlarm(user.getFcmToken(), description, user);
            }

            Optional<Device> device = deviceRepository.findDeviceByRoomIdAndAlias(room.getId(), "에어로타워");
            if(device.isPresent()) thinQService.controlAirPurifierPower(room.getOwner(), device.get().getId(), true); // 공기청정기 켜기

            return "알림이 성공적으로 전송되었습니다.";
        }
    }
    public String firebaseAlarm(String targetToken, String description, User user) {
        Message message = Message.builder()
                .setToken(targetToken)
                .putData("title", "이상치 발생 알림")
                .putData("body", description)
                .build();
        try {
            Notification notification = Notification.builder()
                    .title("이상치 발생 알림")
                    .message(description)
                    .readStatus(false)
                    .user(user)
                    .build();
            notificationRepository.save(notification);
            return FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패. 토큰: {}, 사유: {}", targetToken, e.getMessage());
            return e.getMessagingErrorCode().toString();
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
            level = "예측값보다 상당히 높은 수치입니다. 즉각적인 점검이 필요할 수 있습니다.\n" + descriptionDetail(pollutant);
        } else if (errorRate >= threshold) {
            level = "예측값과 다소 차이가 있는 수치입니다. 주의가 요구됩니다.\n" + descriptionDetail(pollutant);
        } else {
            level = "예측값과 유사하여 정상 범위로 판단됩니다.";
        }


        return String.format("%s 농도가 %.2f로 예측치 %.2f와 비교했을 때, %.0f%% 정도로 %s",
                pollutant, actual, predicted, errorRate*100,level);
    }
    public String descriptionDetail(String pollutant){
        return switch (pollutant.toUpperCase()) {
            case "CO2" -> "CO2 농도가 높아지면 호흡기 건강에 영향을 줄 수 있습니다.\n" +
                    "실내 공기질을 개선하기 위해 환기를 고려하세요.";
            case "TVOC" -> "TVOC 농도가 높아지면 실내 공기질이 저하될 수 있습니다.";
            case "PM10" -> "PM10 농도가 높아지면 호흡기 질환의 위험이 증가할 수 있습니다.";
            default -> "";
        };
    }
    public List<AnomalyReportResponseDto> getAnomalyReports(String serialNumber, LocalDate startDate, LocalDate endDate) {
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, String.format("해당 센서를 찾을 수 없습니다. serialNumber: %s", serialNumber)));

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        List<AnomalyReport> anomalyReports = anomalyReportRepository.findAnomaliesBySensorAndDateRange(sensor, startDateTime, endDateTime);

        return anomalyReports.stream()
                .map(report -> AnomalyReportResponseDto.builder()
                        .sensorSerialNumber(report.getSensor().getSerialNumber())
                        .anomalyTimestamp(report.getAnomalyTimestamp())
                        .pollutant(report.getPollutant().name())
                        .pollutantValue(report.getPollutantValue())
                        .description(report.getDescription())
                        .snapshotData(AnomalyReportResponseDto.SnapshotData.builder()
                                .snapshotTimestamp(report.getRelatedHourlySnapshot().getSnapshotHour())
                                .hourlyAvgTemperature(report.getRelatedHourlySnapshot().getHourlyAvgTemperature())
                                .hourlyAvgHumidity(report.getRelatedHourlySnapshot().getHourlyAvgHumidity())
                                .hourlyAvgPressure(report.getRelatedHourlySnapshot().getHourlyAvgPressure())
                                .hourlyAvgTvoc(report.getRelatedHourlySnapshot().getHourlyAvgTvoc())
                                .hourlyAvgEco2(report.getRelatedHourlySnapshot().getHourlyAvgEco2())
                                .hourlyAvgPm10(report.getRelatedHourlySnapshot().getHourlyAvgPm10())
                                .hourlyAvgPm25(report.getRelatedHourlySnapshot().getHourlyAvgPm25())
                                .scoreData(AnomalyReportResponseDto.ScoreData.builder()
                                        .overallScore(report.getRelatedHourlySnapshot().getOverallScore())
                                        .pm10Score(report.getRelatedHourlySnapshot().getPm10Score())
                                        .pm25Score(report.getRelatedHourlySnapshot().getPm25Score())
                                        .eco2Score(report.getRelatedHourlySnapshot().getEco2Score())
                                        .tvocScore(report.getRelatedHourlySnapshot().getTvocScore())
                                        .build())
                                .build())
                        .build())
                .toList();
    }


}

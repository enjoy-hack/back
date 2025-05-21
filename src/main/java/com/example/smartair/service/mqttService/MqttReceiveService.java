package com.example.smartair.service.mqttService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import com.example.smartair.service.airQualityService.AirQualityDataService;
import com.example.smartair.service.awsFileService.S3Service;
import com.example.smartair.service.sensorService.SensorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttReceiveService {

    private final LinkedList<SensorAirQualityData> recentMessage = new LinkedList<>();
    private final AirQualityDataService airQualityDataService;
    private final AirQualityDataRepository airQualityDataRepository;
    private final RoomSensorRepository roomSensorRepository;
    private final S3Service s3Service;
    private final SensorRepository sensorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    private static final int MAX_QUEUE_SIZE = 300;
    private static final int HOURLY_LIMIT_PER_SENSOR = 100;
    private static final Duration INACTIVITY_THRESHOLD = Duration.ofHours(2); //2시간 이상 데이터 없으면 센서 작동 상태 비활성화 처리
    private final Map<Long, AtomicInteger> sensorMessageCounters = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> sensorLastResetTimes = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 600000) // 10분마다 실행
    @Transactional
    public void checkSensorStatus(){
        LocalDateTime threshold = LocalDateTime.now().minus(INACTIVITY_THRESHOLD);
        List<Sensor> sensors = sensorRepository.findAll();

        for (Sensor sensor : sensors) {
            Optional<SensorAirQualityData> lastData = airQualityDataRepository.findTopBySensorIdOrderByCreatedAtDesc(sensor.getId());

            boolean shouldBeActive = lastData
                    .map(data -> data.getCreatedAt().isAfter(threshold))
                    .orElse(false);

            // 상태가 변경될 때만 업데이트하고 로그 출력
            if (sensor.isRunningStatus() != shouldBeActive) {
                sensor.setRunningStatus(shouldBeActive);
                sensorRepository.save(sensor);
                log.info("센서 일련번호{}의 작동 상태가 {}로 변경되었습니다. 마지막 데이터 수신: {}",
                        sensor.getSerialNumber(), shouldBeActive ? "활성화" : "비활성화",
                        lastData.map(data -> data.getCreatedAt().toString()).orElse("없음"));
            }
        }
    }


    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldData() {
        try {
            LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);
            int deletedCount = airQualityDataRepository.deleteByCreatedAtBefore(eightDaysAgo);
            log.info("데이터 정리 완료: {} 개의 8일 이전 데이터 삭제됨 (기준 일시: {})",
                    deletedCount, eightDaysAgo);
        } catch (Exception e) {
            log.error("데이터 정리 중 오류 발생", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void collectHourlyData() {
        log.info("Starting hourly data collection...");
        try {
            synchronized (recentMessage) {
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                recentMessage.removeIf(data -> data.getCreatedAt().isAfter(oneHourAgo));
            }
        } catch (Exception e) {
            log.error("Error during hourly data collection", e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Transactional
    public AirQualityPayloadDto handleReceiveMessage(String topic, String payload) throws Exception {
        try {
            log.info("Received message on handleReceiveMessage topic '{}', payload: {}", topic, payload);
            String[] topicParts = topic.split("/");

            if (topicParts.length != 3 || !"smartair".equals(topicParts[0]) || !"airquality".equals(topicParts[2])) {
                throw new IllegalArgumentException("토픽 형식은 'smartair/[serialNumber]/airquality' 이어야 합니다.");
            }

            String serialNumber = topicParts[1];

            synchronized (serialNumber.intern()){
            Sensor sensor = sensorRepository.findBySerialNumberWithLock(serialNumber)
                    .orElseThrow(() -> new EntityNotFoundException("Sensor serialNumber: " + serialNumber));

            //센서가 비활성상태였다면 활성화
            if (!sensor.isRunningStatus()) {
                sensor.setRunningStatus(true);
                sensorRepository.save(sensor);
                log.info("센서 일련번호 {}가 데이터 수신을 시작하여 활성화되었습니다.", serialNumber);
            }

            Long sensorId = sensor.getId();

            Long roomId = roomSensorRepository.findBySensor_Id(sensor.getId())
                    .map(roomSensor -> roomSensor.getRoom().getId())
                    .orElse(null);

            if (isRateLimitExceeded(sensorId)) {
                throw new IllegalStateException("Sensor serialNumber: " + serialNumber + " (제한: " + HOURLY_LIMIT_PER_SENSOR + "/hour)");
            }

            s3Service.uploadJson(serialNumber, payload);

            log.info("JSON 파싱 시작: {}", payload);
            AirQualityPayloadDto dto = objectMapper.readValue(payload, AirQualityPayloadDto.class);
            log.info("JSON 파싱 완료: {}", dto);

            AirQualityPayloadDto processedDto = airQualityDataService.processAirQualityData(sensorId, roomId, dto);

            SensorAirQualityData sensorData = airQualityDataService.getSavedAirQualityData(sensorId);
            if (sensorData != null) {
                addToMessageQueue(sensorData);
            }

            return processedDto; }
        }  catch (ResponseStatusException e) {
            throw e;  // ResponseStatusException을 그대로 전달
        }
    }


    private boolean isRateLimitExceeded(Long sensorId) {
        LocalDateTime now = LocalDateTime.now();
        sensorLastResetTimes.computeIfAbsent(sensorId, k -> now);
        sensorMessageCounters.computeIfAbsent(sensorId, k -> new AtomicInteger(0));

        if (Duration.between(sensorLastResetTimes.get(sensorId), now).toHours() >= 1) {
            sensorMessageCounters.get(sensorId).set(0);
            sensorLastResetTimes.put(sensorId, now);
            log.debug("센서 ID: {} 의 메시지 카운터를 리셋했습니다.", sensorId);
        }

        int currentCount = sensorMessageCounters.get(sensorId).incrementAndGet();
        return currentCount > HOURLY_LIMIT_PER_SENSOR;
    }

    private void addToMessageQueue(SensorAirQualityData sensorData) {
        synchronized (recentMessage) {
            while (recentMessage.size() >= MAX_QUEUE_SIZE) {
                SensorAirQualityData removed = recentMessage.removeFirst();
                log.debug("Queue full, removed oldest message for Device ID: {}",
                        removed.getSensor().getId());
            }
            recentMessage.addLast(sensorData);
            log.debug("Added new message to queue for Device ID: {}. Queue size: {}",
                    sensorData.getSensor().getId(), recentMessage.size());
        }
    }

    public List<SensorAirQualityData> getRecentMessage() {
        return List.copyOf(recentMessage);
    }
}
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
    private final Map<Long, AtomicInteger> sensorMessageCounters = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> sensorLastResetTimes = new ConcurrentHashMap<>();


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

    public AirQualityPayloadDto handleReceiveMessage(String topic, String payload) throws Exception {
        try {
            log.info("Received message on handleReceiveMessage topic '{}', payload: {}", topic, payload);
            String[] topicParts = topic.split("/");

            if (topicParts.length != 3 || !"smartair".equals(topicParts[0]) || !"airquality".equals(topicParts[2])) {
                throw new IllegalArgumentException("토픽 형식은 'smartair/[sensorId]/airquality' 이어야 합니다.");
            }

            Long deviceId = Long.parseLong(topicParts[1]);
            Sensor sensor = sensorRepository.findById(deviceId)
                    .orElseThrow(() -> new EntityNotFoundException("Sensor ID: " + deviceId));

            Long roomId = roomSensorRepository.findBySensor_Id(sensor.getId())
                    .map(roomSensor -> roomSensor.getRoom().getId())
                    .orElse(null);

            if (isRateLimitExceeded(deviceId)) {
                throw new IllegalArgumentException("Sensor ID: " + deviceId + " (제한: " + HOURLY_LIMIT_PER_SENSOR + "/hour)");
            }

            s3Service.uploadJson(deviceId.toString(), payload);

            log.info("JSON 파싱 시작: {}", payload);
            AirQualityPayloadDto dto = objectMapper.readValue(payload, AirQualityPayloadDto.class);
            log.info("JSON 파싱 완료: {}", dto);

            AirQualityPayloadDto processedDto = airQualityDataService.processAirQualityData(deviceId, roomId, dto);

            SensorAirQualityData sensorData = airQualityDataService.getSavedAirQualityData(deviceId);
            if (sensorData != null) {
                addToMessageQueue(sensorData);
            }

            return processedDto;
        }  catch (ResponseStatusException e) {
            throw e;  // ResponseStatusException을 그대로 전달
        }
    }


    private boolean isRateLimitExceeded(Long deviceId) {
        LocalDateTime now = LocalDateTime.now();
        sensorLastResetTimes.computeIfAbsent(deviceId, k -> now);
        sensorMessageCounters.computeIfAbsent(deviceId, k -> new AtomicInteger(0));

        if (Duration.between(sensorLastResetTimes.get(deviceId), now).toHours() >= 1) {
            sensorMessageCounters.get(deviceId).set(0);
            sensorLastResetTimes.put(deviceId, now);
            log.debug("센서 ID: {} 의 메시지 카운터를 리셋했습니다.", deviceId);
        }

        int currentCount = sensorMessageCounters.get(deviceId).incrementAndGet();
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
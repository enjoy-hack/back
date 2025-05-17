package com.example.smartair.service.mqttService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.service.airQualityService.AirQualityDataService;
import com.example.smartair.service.awsFileService.S3Service;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final int MAX_QUEUE_SIZE = 300; // 최대 큐 사이즈
    private static final int HOURLY_LIMIT_PER_SENSOR = 100; // 센서당 시간당 최대 데이터 수
    private final Map<Long, AtomicInteger> sensorMessageCounters = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> sensorLastResetTimes = new ConcurrentHashMap<>();

    // 매일 새벽 3시에 8일 간격으로 공기질 데이터 삭제 (서버 부하가 적은 시간)
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldData() {
        try {
            LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);

            // 8일 이전 데이터 삭제
            int deletedCount = airQualityDataRepository.deleteByCreatedAtBefore(eightDaysAgo);

            log.info("데이터 정리 완료: {} 개의 8일 이전 데이터 삭제됨 (기준 일시: {})",
                    deletedCount, eightDaysAgo);
        } catch (Exception e) {
            log.error("데이터 정리 중 오류 발생", e);
        }
    }


    @Scheduled(fixedRate = 3600000) // 1시간 간격
    public void collectHourlyData() {
        log.info("Starting hourly data collection...");

        try {
            synchronized (recentMessage) { //실시간으로 수집된 모든 센서 데이터가 있는 LinkedList
                LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                // S3에 1시간 시간별 데이터(집계된 데이터)를 업로드하고 싶으면 주석을 해제
                // 센서별로 데이터 그룹화
//                Map<Long, List<SensorAirQualityData>> sensorDataMap = recentMessage.stream()
//                        .filter(data -> data.getCreatedAt().isAfter(oneHourAgo)) //최근 1시간 데이터만 필터링
//                        .collect(Collectors.groupingBy(data -> data.getSensor().getId())); //센서ID기준으로 그룹화

//                // 센서별 데이터 처리
//                sensorDataMap.forEach((sensorId, dataList) -> {
//                    try {
//                       String jsonData = objectMapper.writeValueAsString(dataList);
//
//                       String deviceId = sensorId.toString();
//                       String roomId = dataList.get(0).getSensor().getRoom().getId().toString();
//
//                       //s3 upload
//                        s3Service.uploadJson(deviceId, roomId, jsonData);
//                        log.info("Processed and uploaded {} records for sensor {}", dataList.size(), sensorId);
//                }
//                    catch (Exception e) {
//                        log.error("Error processing data for sensor {}: {}", sensorId, e.getMessage());
//                    }
//                });

                // 처리된 데이터 제거
                recentMessage.removeIf(data -> data.getCreatedAt().isAfter(oneHourAgo));
            }

        } catch (Exception e) {
            log.error("Error during hourly data collection", e);
            throw new CustomException(ErrorCode.MQTT_PROCESSING_ERROR);
        }
    }


    public AirQualityPayloadDto handleReceiveMessage(String topic, String payload) {
        try {
            log.info("Received message on handleReceiveMessage topic '{}', payload: {}", topic, payload);

            // 토픽 구조: smartair/{deviceId}/{roomId}/airquality
            String[] topicParts = topic.split("/");
            
            // 유효성 검사: smartair로 시작하고, 총 4개의 파트가 있으며, 마지막은 airquality인지 확인
            if (topicParts.length == 4 && "smartair".equals(topicParts[0]) && "airquality".equals(topicParts[3])) {
                // deviceId와 roomId를 String으로 추출
                String deviceIdString = topicParts[1]; 
                String roomIdString = topicParts[2];
                Long deviceId = Long.parseLong(deviceIdString);
                Long roomId = Long.parseLong(roomIdString);

                //센서별 제한 체크
                if (isRateLimitExceeded(deviceId)){
                    log.warn("시간당 메시지 제한 ({})을 초과했습니다. 센서 ID: {}",
                            HOURLY_LIMIT_PER_SENSOR, deviceId);
                    throw new CustomException(ErrorCode.MQTT_RATE_LIMIT_EXCEEDED);
                }

                // 실시간으로 들어오는 원본 데이터를 바로 업로드
                s3Service.uploadJson(deviceIdString, roomIdString, payload); 

                AirQualityPayloadDto dto = objectMapper.readValue(payload, AirQualityPayloadDto.class);

                // 데이터 처리 및 저장
                AirQualityPayloadDto processedDto = airQualityDataService.processAirQualityData(deviceId, roomId, dto);

                // 처리된 데이터를 SensorAirQualityData로 변환하여 큐에 추가
                SensorAirQualityData sensorData = airQualityDataService.getSavedAirQualityData(deviceId);
                if (sensorData != null) {
                    addToMessageQueue(sensorData);
                }

                return processedDto;

            } else {
                log.warn("Received message on unexpected topic format: {}", topic);
                throw new CustomException(ErrorCode.MQTT_INVALID_TOPIC_ERROR); 
            }

        } catch (NumberFormatException nfe) { // Long.parseLong에서 발생할 수 있는 예외 처리
            log.error("Error parsing deviceId or roomId from topic: {}", topic, nfe);
            throw new CustomException(ErrorCode.MQTT_INVALID_TOPIC_ERROR); 
        } catch (CustomException ce) {
            throw ce;
        } catch (com.fasterxml.jackson.core.JsonProcessingException jpe) {
            log.error("Error parsing JSON payload: Payload={}", payload, jpe);
            throw new CustomException(ErrorCode.MQTT_JSON_PARSING_ERROR); 
        } catch (Exception e) {
            log.error("Error handling MQTT message: Topic={}, Payload={}", topic, payload, e);
            throw new CustomException(ErrorCode.MQTT_PROCESSING_ERROR);
        }
    }

    private boolean isRateLimitExceeded(Long deviceId) {
        LocalDateTime now = LocalDateTime.now();

        // 센서별 카운터 초기화 체크
        sensorLastResetTimes.computeIfAbsent(deviceId, k -> now);
        sensorMessageCounters.computeIfAbsent(deviceId, k -> new AtomicInteger(0));

        // 1시간이 지났으면 카운터 리셋
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

    public List<SensorAirQualityData> getRecentMessage(){
        return List.copyOf(recentMessage);
    }

}

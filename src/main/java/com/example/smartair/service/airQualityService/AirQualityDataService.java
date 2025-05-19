package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirQualityDataService {

    private final SensorRepository sensorRepository;
    private final RoomSensorRepository roomSensorRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final FineParticlesDataRepository fineParticlesDataRepository;
    private final RecentAirQualityDataCache recentAirQualityDataCache;
    private final FineParticlesDataPt2Repository fineParticlesDataPt2Repository;
    private final AirQualityScoreService airQualityScoreService;

    @Transactional
    public AirQualityPayloadDto processAirQualityData(Long deviceId, Long roomId, AirQualityPayloadDto dto) {
        try {
            // 1. Device 추출
            Sensor sensor = sensorRepository.findById(deviceId)
                    .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "sensor Id: " + deviceId));

            // 2. Room 정보는 옵셔널하게 처리
            Room room = null;
            if (roomId != null){
                room = roomSensorRepository.findBySensor(sensor)
                        .map(RoomSensor::getRoom)
                        .orElse(null);
                if (room != null) {
                    log.info("센서 ID {}가 방 ID {}에 매핑되어 있습니다.", deviceId, room.getId());
                }
            }

            // 3. FineParticlesData 엔티티 생성 및 저장
            FineParticlesData fineParticlesData = dto.toPt1Entity();
            FineParticlesData savedFineParticlesData = fineParticlesDataRepository.save(fineParticlesData);

            FineParticlesDataPt2 fineParticlesDataPt2 = dto.toPt2Entity();
            FineParticlesDataPt2 savedFineParticlesDataPt2 = fineParticlesDataPt2Repository.save(fineParticlesDataPt2);

            // 4. AirQualityData 엔티티 생성
            SensorAirQualityData airQualityData = SensorAirQualityData.builder()
                    .temperature(dto.getTemperature())
                    .humidity(dto.getHumidity())
                    .pressure(dto.getPressure())
                    .tvoc(dto.getTvoc())
                    .eco2(dto.getEco2())
                    .rawh2(dto.getRawh2())
                    .rawethanol(dto.getRawethanol())
                    .sensor(sensor)
                    .fineParticlesData(savedFineParticlesData)
                    .fineParticlesDataPt2(savedFineParticlesDataPt2)
                    .build();

            // 5. AirQualityData 저장
            SensorAirQualityData savedAirQualityData = airQualityDataRepository.save(airQualityData);

            // 즉시 점수 계산
            airQualityScoreService.calculateAndSaveDeviceScore(savedAirQualityData);

            // 7. 캐싱
            updateCache(sensor.getId(), savedAirQualityData);

            return dto;

        } catch (CustomException ce) {
            log.error("비즈니스 로직 오류: {}", ce.getMessage());
            throw ce;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("MQTT 토픽 구문 분석 또는 처리 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.MQTT_PROCESSING_ERROR, String.format("MQTT 처리 오류: %s", e.getMessage()));
        } catch (Exception e) {
            log.error("MQTT 데이터 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, String.format("내부 서버 오류: %s", e.getMessage()));

        }
    }

    @Transactional(readOnly = true)
    public SensorAirQualityData getSavedAirQualityData(Long deviceId) {
        try {
            // 1. 캐시에서 먼저 조회
            Optional<SensorAirQualityData> cachedData = recentAirQualityDataCache.get(deviceId);
            if (cachedData.isPresent()) {
                log.debug("Cache hit for device ID: {}", deviceId);
                return cachedData.get();
            }

            // 2. 캐시에 없는 경우 DB에서 최신 데이터 조회
            SensorAirQualityData latestData = airQualityDataRepository.findTopBySensorIdOrderByCreatedAtDesc(deviceId)
                    .orElseThrow(() -> {
                        log.warn("Device ID {}의 최근 데이터를 찾을 수 없습니다.", deviceId);
                        return new CustomException(ErrorCode.SENSOR_AIR_DATA_NOT_FOUND, "Sensor ID {}" + deviceId);
                    });

            // 3. 조회된 데이터를 캐시에 저장
            updateCache(deviceId, latestData);
            log.debug("Latest data cached for device ID: {}", deviceId);

            return latestData;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Device ID {}의 데이터 조회 중 오류 발생", deviceId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, String.format("내부 서버 오류: %s", e.getMessage()));
        }
    }

    // 캐시 관리를 위한 전용 메서드
    private void updateCache(Long sensorId, SensorAirQualityData data) {
        try {
            recentAirQualityDataCache.put(sensorId, data);
            log.debug("Cache updated for sensor ID: {}", sensorId);
        } catch (Exception e) {
            log.error("Cache update failed for sensor ID: {}", sensorId, e);
            // 캐시 실패는 애플리케이션 동작에 영향을 주지 않도록 처리
        }
    }


}

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
    public AirQualityPayloadDto processAirQualityData(Long deviceId, Long roomIdFromTopic, AirQualityPayloadDto dto) {
        try {

            // 1. Device 추출
            Sensor sensor = sensorRepository.findById(deviceId)
                    .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

            // 2. Room 추출
            Room actualRoom = roomSensorRepository.findBySensor(sensor)
                    .map(RoomSensor::getRoom)
                    .orElseThrow(() -> new CustomException(ErrorCode.ROOM_DEVICE_MAPPING_NOT_FOUND));

            // 토픽에서 파싱된 roomId와 DB에서 조회한 실제 Room의 ID가 일치하는지 확인
            if (!actualRoom.getId().equals(roomIdFromTopic)) {
                log.warn("토픽의 Room ID({})와 실제 Device ID {} 가 DB에 매핑된 Room ID({})와 불일치합니다. DB에 매핑된 Room ID '{}'를 기준으로 처리합니다.",
                        roomIdFromTopic, deviceId, actualRoom.getId(), actualRoom.getId());
            }

            // 4. FineParticlesData 엔티티 생성 및 저장 
            FineParticlesData fineParticlesData = FineParticlesData.builder()
                    .pm10_standard(dto.getPt1Pm10Standard())
                    .pm25_standard(dto.getPt1Pm25Standard())
                    .pm100_standard(dto.getPt1Pm100Standard())
                    .particle_03(dto.getPt1Particles03um())
                    .particle_05(dto.getPt1Particles05um())
                    .particle_10(dto.getPt1Particles10um())
                    .particle_25(dto.getPt1Particles25um())
                    .particle_50(dto.getPt1Particles50um())
                    .particle_100(dto.getPt1Particles100um())
                    .sensor(sensor) // Device 연결
                    .build();
            FineParticlesData savedFineParticlesData = fineParticlesDataRepository.save(fineParticlesData);

            FineParticlesDataPt2 fineParticlesDataPt2 = FineParticlesDataPt2.builder()
                    .pm10_standard(dto.getPt2Pm10Standard())
                    .pm25_standard(dto.getPt2Pm25Standard())
                    .pm100_standard(dto.getPt2Pm100Standard())
                    .particle_03(dto.getPt2Particles03um())
                    .particle_05(dto.getPt2Particles05um())
                    .particle_10(dto.getPt2Particles10um())
                    .particle_25(dto.getPt2Particles25um())
                    .particle_50(dto.getPt2Particles50um())
                    .particle_100(dto.getPt2Particles100um())
                    .sensor(sensor) // Device 연결
                    .build();
            FineParticlesDataPt2 savedFineParticlesDataPt2 = fineParticlesDataPt2Repository.save(fineParticlesDataPt2);

            // 5. AirQualityData 엔티티 생성
            SensorAirQualityData airQualityData = SensorAirQualityData.builder()
                    .temperature(dto.getTemperature())
                    .humidity(dto.getHumidity())
                    .pressure(dto.getPressure())
                    .tvoc(dto.getTvoc())
                    .eco2(dto.getPpm())
                    .rawh2(dto.getRawh2())
                    .rawethanol(dto.getRawethanol())
                    .sensor(sensor)
                    .fineParticlesData(savedFineParticlesData)
                    .fineParticlesDataPt2(savedFineParticlesDataPt2)
                    .build();

            // 6. AirQualityData 저장
            SensorAirQualityData savedAirQualityData = airQualityDataRepository.save(airQualityData);

            //즉시 점수 계산
            airQualityScoreService.calculateAndSaveDeviceScore(savedAirQualityData);

            // 7. 캐싱
            updateCache(sensor.getId(), savedAirQualityData);

            return dto;

        } catch (CustomException ce) {
            log.error("비즈니스 로직 오류: {}", ce.getMessage());
            throw ce;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            log.error("MQTT 토픽 구문 분석 또는 처리 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.MQTT_PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("MQTT 데이터 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);

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
                        return new CustomException(ErrorCode.SENSOR_AIR_DATA_NOT_FOUND);
                    });

            // 3. 조회된 데이터를 캐시에 저장
            updateCache(deviceId, latestData);
            log.debug("Latest data cached for device ID: {}", deviceId);

            return latestData;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Device ID {}의 데이터 조회 중 오류 발생", deviceId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
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

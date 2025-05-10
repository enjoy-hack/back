package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.Sensor.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomDevice;
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

    @Transactional
    public AirQualityPayloadDto processAirQualityData(String topic, AirQualityPayloadDto dto) {
        try {
            String[] parts = topic.split("/");
            if (parts.length < 3){
                log.error("잘못된 토픽 형식 수신 : {}", topic);
                throw new CustomException(ErrorCode.MQTT_INVALID_TOPIC_ERROR);
            }
            // 2. Device 추출
            Long deviceId = Long.parseLong(topic.split("/")[1]);
            Device device = sensorRepository.findById(deviceId)
                    .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

            // 3. Room 추출
            Long roomIdFromTopic = Long.parseLong(topic.split("/")[2]);
            Room room = roomSensorRepository.findByDevice(device)
                    .map(RoomDevice::getRoom)
                    .orElseThrow(() -> new CustomException(ErrorCode.ROOM_DEVICE_MAPPING_NOT_FOUND));

            if (!room.getId().equals(roomIdFromTopic)){
                log.warn("토픽의 Room ID({})와 실제 Device({})가 매핑된 Room ID({}) 불일치", roomIdFromTopic, deviceId, room.getId());
            }


            // 4. FineParticlesData 엔티티 생성 및 저장 (pt1 데이터 기준)
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
                    .device(device) // Device 연결
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
                    .device(device) // Device 연결
                    .build();
            FineParticlesDataPt2 savedFineParticlesDataPt2 = fineParticlesDataPt2Repository.save(fineParticlesDataPt2);

            // 5. AirQualityData 엔티티 생성
            DeviceAirQualityData airQualityData = DeviceAirQualityData.builder()
                    .topic(topic)
                    .temperature(dto.getTemperature())
                    .humidity(dto.getHumidity())
                    .pressure(dto.getPressure())
                    .tvoc(dto.getTvoc())
                    .eco2(dto.getPpm()) 
                    .rawh2(dto.getRawh2())
                    .rawethanol(dto.getRawethanol())
                    .device(device)
                    .fineParticlesData(savedFineParticlesData)
                    .fineParticlesDataPt2(savedFineParticlesDataPt2)
                    .build();

            // 6. AirQualityData 저장
            DeviceAirQualityData savedAirQualityData = airQualityDataRepository.save(airQualityData);

            // 7. 캐싱
            recentAirQualityDataCache.put(device.getId(), savedAirQualityData);

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

    // 캐시에 저장된 데이터 조회 메서드
    public Optional<DeviceAirQualityData> getRecentAirQualityData(Long deviceId) {
        return recentAirQualityDataCache.get(deviceId);
    }
}

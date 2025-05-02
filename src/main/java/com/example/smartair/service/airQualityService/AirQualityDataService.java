package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.roomDeviceRepository.RoomDeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AirQualityDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeviceRepository deviceRepository;
    private final RoomDeviceRepository roomDeviceRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final FineParticlesDataRepository fineParticlesDataRepository;
    private final RecentAirQualityDataCache recentAirQualityDataCache;

    @Transactional
    public AirQualityPayloadDto processAirQualityData(String topic, String payload) {
        try {
            // 1. JSON 파싱 
            AirQualityPayloadDto dto = objectMapper.readValue(payload, AirQualityPayloadDto.class);

            // 2. Device 및 Room 찾기
            Long deviceId = Long.parseLong(topic.split("/")[1]);
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new CustomException(ErrorCode.DEVICE_NOT_FOUND));
            Room room = roomDeviceRepository.findByDevice(device)
                    .map(RoomDevice::getRoom)
                    .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

            // 3. FineParticlesData 엔티티 생성 및 저장 (pt1 데이터 기준)
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

            // 4. AirQualityData 엔티티 생성 
            DeviceAirQualityData airQualityData = DeviceAirQualityData.builder()
                    .topic(topic)
                    .payload(payload) // 원본 페이로드 저장
                    .temperature(dto.getTemperature())
                    .humidity(dto.getHumidity())
                    .pressure(dto.getPressure())
                    .tvoc(dto.getTvoc())
                    .eco2(dto.getPpm()) 
                    .rawh2(dto.getRawh2())
                    .rawethanol(dto.getRawethanol())
                    .device(device)
                    .fineParticlesData(savedFineParticlesData) 
                    .build();

            // 5. AirQualityData 저장
            DeviceAirQualityData savedAirQualityData = airQualityDataRepository.save(airQualityData);

            // 6. 캐싱 
            recentAirQualityDataCache.put(device.getId(), savedAirQualityData);

            return dto; 
        } catch (Exception e) {
            log.error("MQTT Payload 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.MQTT_PROCESSING_ERROR);
        }
    }

    // 캐시에 저장된 데이터 조회 메서드
    public Optional<DeviceAirQualityData> getRecentAirQualityData(Long deviceId) {
        return recentAirQualityDataCache.get(deviceId);
    }
}

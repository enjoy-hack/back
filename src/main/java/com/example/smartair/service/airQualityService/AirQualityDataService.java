package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.AirQualityData;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.infrastructure.RecentAirQualityDataCache;
import com.example.smartair.repository.AirQualityDataRepository;
import com.example.smartair.repository.DeviceRepository;
import com.example.smartair.repository.RoomDeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AirQualityDataService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DeviceRepository deviceRepository;
    private final RoomDeviceRepository roomDeviceRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final RecentAirQualityDataCache recentAirQualityDataCache;

    public AirQualityDataService(DeviceRepository deviceRepository, RoomDeviceRepository roomDeviceRepository, AirQualityDataRepository airQualityDataRepository, RecentAirQualityDataCache recentAirQualityDataCache) {
        this.deviceRepository = deviceRepository;
        this.roomDeviceRepository = roomDeviceRepository;
        this.airQualityDataRepository = airQualityDataRepository;
        this.recentAirQualityDataCache = recentAirQualityDataCache;
    }

    public AirQualityPayloadDto processAirQualityData(String topic, String payload) {
        try{
            //json parsing
            AirQualityPayloadDto dto = objectMapper.readValue(payload, AirQualityPayloadDto.class);

            //topic에서 deviceId parsing
            Long deviceId = Long.parseLong(topic.split("/")[1]);
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(()->new RuntimeException("Device not found"));

            //room 찾기
            Room room = roomDeviceRepository.findByDevice(device)
                    .map(RoomDevice::getRoom)
                    .orElseThrow(()->new RuntimeException("Room not found"));

            //entity 생성
            AirQualityData data = AirQualityData.builder()
                    .topic(topic)
                    .payload(payload)
                    .temperature(dto.getTemperature())
                    .humidity(dto.getHumidity())
                    .pressure(dto.getPressure())
                    .tvoc(dto.getTvoc())
                    .ppm(dto.getPpm())
                    .rawh2(dto.getRawh2())
                    .rawethanol(dto.getRawethanol())
                    .device(device)
                    .room(room)
                    .build();

            //저장
            airQualityDataRepository.save(data);

            //캐싱
            recentAirQualityDataCache.put(device.getId(), data);
            return dto;
        }
        catch (Exception e){
            log.error("MQTT Payload 처리 중 오류 발생", e);
            return null;
        }
    }

    public Optional<AirQualityData> getRecentAirQualityData(Long deviceId) {
        return recentAirQualityDataCache.get(deviceId);
    }
}

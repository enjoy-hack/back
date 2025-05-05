package com.example.smartair.service.mqttService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.service.airQualityService.AirQualityDataService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttReceiveService {

    private final LinkedList<DeviceAirQualityData> recentMessage = new LinkedList<>();
    private final AirQualityDataService airQualityDataService;

    public AirQualityPayloadDto handleReceiveMessage(String topic, AirQualityPayloadDto dto) {
        try {
            log.info("Received message on topic '{}'", topic);
            return airQualityDataService.processAirQualityData(topic, dto);
        } catch (Exception e) {
            log.error("Error handling MQTT message: Topic={}, Payload={}", topic, dto, e);
            throw new CustomException(ErrorCode.MQTT_PROCESSING_ERROR);
        }
    }

//    public Long extractDeviceIdFromTopic(String topic) {
//        try{
//            return Long.parseLong(topic.split("/")[1]);
//        }
//        catch (Exception e){
//            throw new IllegalArgumentException("올바르지 않은 topic 형식입니다. " + topic);
//        }
//    }

    public List<DeviceAirQualityData> getRecentMessage(){
        return List.copyOf(recentMessage);
    }

//    private double getValue(String keyValue){
//        return Double.parseDouble(keyValue.split("=")[1]);
//    }
}

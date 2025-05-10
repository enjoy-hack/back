package com.example.smartair.service.mqttService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.service.airQualityService.AirQualityDataService;
import com.example.smartair.service.awsFileService.S3Service;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttReceiveService {

    private final LinkedList<DeviceAirQualityData> recentMessage = new LinkedList<>();
    private final AirQualityDataService airQualityDataService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AirQualityPayloadDto handleReceiveMessage(String topic, String payload) {
        try {

            log.info("Received message on topic '{}', payload: {}", topic, payload);

            String deviceId = topic.split("/")[1];
            String roomId = topic.split("/")[2];
            s3Service.uploadJson(deviceId, roomId, payload);

            //String payload를 JSON으로 파싱 -> AirQualityPayloadDto로 변환
            AirQualityPayloadDto dto = objectMapper.readValue(payload, AirQualityPayloadDto.class);

            return airQualityDataService.processAirQualityData(topic, dto);
        } catch (Exception e) {
            log.error("Error handling MQTT message: Topic={}, Payload={}", topic, payload, e);
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

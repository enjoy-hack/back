package com.example.smartair.service.mqttService;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.service.airQualityService.AirQualityDataService;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class MqttReceiveService {

    private final LinkedList<DeviceAirQualityData> recentMessage = new LinkedList<>();
    private final AirQualityDataService airQualityDataService;

    public MqttReceiveService(AirQualityDataService airQualityDataService) {
        this.airQualityDataService = airQualityDataService;
    }


    public AirQualityPayloadDto handleReceiveMessage(String topic, String payload){
        return airQualityDataService.processAirQualityData(topic, payload);

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

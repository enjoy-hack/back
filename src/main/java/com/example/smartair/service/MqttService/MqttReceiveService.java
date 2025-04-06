package com.example.smartair.service.MqttService;

import org.springframework.stereotype.Service;

@Service
public class MqttReceiveService {

    public void handleReceiveMessage(String topic, String payload){
        System.out.println("[서비스] 받은 메시지");
        System.out.println("Topic : " + topic);
        System.out.println("Payload : " + payload);
    }
}

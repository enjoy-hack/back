package com.example.smartair.service.mqttService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class MqttSendService {

    @Autowired
    @Qualifier("mqttOutboundChannel") //config에서 선언한 채널과 매핑
    private MessageChannel mqttOutboundChannel;

    //Spring에서 외부로 메시지 전달
    public void sendToMqtt(String data, String topic){
        Message<String> message = MessageBuilder
                .withPayload(data)
                .setHeader(MqttHeaders.TOPIC, topic)
                .build();

        mqttOutboundChannel.send(message);
        System.out.println("MQTT 메시지 전송 완료 -> Topic : " + topic + ", Payload : " + data);
    }


}

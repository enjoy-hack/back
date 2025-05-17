package com.example.smartair.config;

import com.example.smartair.dto.airQualityDataDto.AirQualityPayloadDto;

import com.example.smartair.service.mqttService.MqttReceiveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageHandler;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;

import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;


@Configuration
@Slf4j
public class MqttConfig {

    @Value("${mqtt.url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic}")
    private String topic;

    //client factory
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{brokerUrl});
        factory.setConnectionOptions(options);
        return factory;
    }

    //외부 클라이언트 -> MQTT Broker -> Spring 으로 메시지 전송
    @Bean
    public MessageProducer inbound(){
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(brokerUrl, clientId, topic);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        adapter.setCompletionTimeout(5000);
        log.info("MQTT inbound adapter created with topic: {}", topic);
        return adapter;
    }

    //MQTT 구독 핸들러
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(MqttReceiveService mqttReceiveService) {
        return message -> {
            try {
                String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
                String payload = (String) message.getPayload();
                log.info("Received MQTT message - Topic: {}, Payload: {}", topic, payload);
                mqttReceiveService.handleReceiveMessage(topic, payload);
            } catch (Exception e) {
                log.error("Error handling MQTT message: ", e);
            }
        };
    }

    //MQTT 구독 채널 생성
    @Bean
    public MessageChannel mqttInputChannel(){
        DirectChannel channel = new DirectChannel();
        channel.setDatatypes(String.class);
        return channel;
    }
}

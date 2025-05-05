package com.example.smartair.controller.mqttController;
import com.example.smartair.service.MqttService.MqttSendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mqtt/send")
public class MqttSendController {
    private final MqttSendService mqttSendService;

    public MqttSendController(MqttSendService mqttSendService) {
        this.mqttSendService = mqttSendService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(@RequestParam String topic, @RequestParam String message){
        mqttSendService.sendToMqtt(topic, message);
        return ResponseEntity.ok("메시지 전송 완료, topic : " + topic + ", message : " + message);
    }
}

package com.example.smartair.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mqtt/receive")
public class MqttReceiveController {

    @GetMapping("/ping")
    public String ping(){
        return "MQTT 수신 컨트롤러 정상 작동 중";
    }
}

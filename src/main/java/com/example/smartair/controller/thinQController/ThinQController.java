package com.example.smartair.controller.thinQController;

import com.example.smartair.service.thinQService.ThinQService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ThinQController {

    private final ThinQService thinQService;

    public ThinQController(ThinQService thinQService) {
        this.thinQService = thinQService;
    }

    @GetMapping("/thinq/devices")
    public ResponseEntity<String> getDevices() {
        return thinQService.getDeviceList();
    }

    @GetMapping("/thinq/{deviceId}/status")
    public ResponseEntity<String> getDeviceStatus(@PathVariable("deviceId") String deviceId) {
        return thinQService.getDeviceStatus(deviceId);
    }

    @PostMapping("/thinq/{deviceId}/power")
    public ResponseEntity<String> controlPower(
            @PathVariable("deviceId") String deviceId) throws JsonProcessingException {
        return thinQService.controlAirPurifierPower(deviceId);
    }

}


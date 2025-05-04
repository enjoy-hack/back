package com.example.smartair.controller.airQualityController;

import com.example.smartair.dto.airQualityDataDto.AirQualityUploadRequest;
import com.example.smartair.service.awsFileService.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class AirQualityController {

    private final S3Service s3Service;

    // S3 업로드 API
    @PostMapping("/upload")
    public ResponseEntity<?> uploadData(@RequestBody AirQualityUploadRequest request) throws Exception {
        String key = s3Service.uploadJson(request.getDeviceId(), request.getJsonPayload());
        return ResponseEntity.ok().body("Uploaded to S3: " + key);
    }
}

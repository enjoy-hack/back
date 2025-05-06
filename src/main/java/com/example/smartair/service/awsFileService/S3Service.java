package com.example.smartair.service.awsFileService;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    // JSON 데이터 업로드
    public String uploadJson(String deviceId, String jsonPayload) throws Exception {
        try{
        String today = LocalDate.now().toString(); // ex: 2025-05-04
        String key = String.format("airQuality/%s/%s.json", today, deviceId);

        InputStream inputStream = new ByteArrayInputStream(jsonPayload.getBytes(StandardCharsets.UTF_8));

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/json");
        metadata.setContentLength(jsonPayload.length());

        amazonS3Client.putObject(new PutObjectRequest(bucket, key, inputStream, metadata));

        return key;
        } catch (AmazonS3Exception e) {
            log.error("S3 Upload Error: {}", e.getErrorMessage());
            throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

}

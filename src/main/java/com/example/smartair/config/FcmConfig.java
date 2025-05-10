package com.example.smartair.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FcmConfig {

    @Value("${firebase.config.path}")
    private Resource firebaseConfigResource; // resource로 주입

    @PostConstruct
    public void initializeFirebase() throws IOException {
        InputStream serviceAccountStream = firebaseConfigResource.getInputStream(); // Resource 사용

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                .build();

        if (FirebaseApp.getApps().isEmpty()) { // 이미 초기화됐는지 확인
            FirebaseApp.initializeApp(options);
        }
    }
}
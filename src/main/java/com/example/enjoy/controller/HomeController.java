package com.example.enjoy.controller;

import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HomeController {

    private final TrackService trackService;

    @GetMapping("/home")
    public List<TrackProgressDto> getProgress() {
        // 1. 반환 타입을 List<TrackProgressDto>로 변경
        String currentStudentId = "1";
        // 2. 서비스 호출 후 데이터를 바로 반환
        return trackService.calculateTrackProgress(currentStudentId);
    }
}

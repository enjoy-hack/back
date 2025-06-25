package com.example.enjoy.controller;

import com.example.enjoy.dto.TrackProgressDto;
import com.example.enjoy.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "홈 컨트롤러", description = "메인 화면 API")
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final TrackService trackService;

    @Operation(summary = "트랙 진행률 조회", description = "현재 학생의 전체 트랙 진행률을 조회합니다.")
    @GetMapping("/home/{studentId}")
    public List<TrackProgressDto> showMyProgress(@PathVariable String studentId) {
        return trackService.calculateTrackProgress(studentId);
    }
}
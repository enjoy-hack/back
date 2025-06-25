package com.example.enjoy.controller;

import com.example.enjoy.dto.TrackDetailDto;
import com.example.enjoy.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackService trackService;

    /**
     * 특정 트랙의 상세 정보를 조회하는 API
     * @param trackId 조회할 트랙의 ID
     * @return TrackDetailDto - 트랙의 상세 정보
     */
    @GetMapping("/{trackId}")
    public TrackDetailDto getTrackDetailsById(@PathVariable Long trackId) {

        // TODO: 추후 Spring Security 연동 후 실제 로그인한 학생 ID를 가져와야 함
        Long currentStudentId = 1L;

        // 5. 서비스의 메서드를 호출하여 결과를 받아온 후, 그대로 반환
        return trackService.getTrackDetails(currentStudentId, trackId);
    }
}

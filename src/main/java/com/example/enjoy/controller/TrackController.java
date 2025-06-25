package com.example.enjoy.controller;

import com.example.enjoy.dto.TrackDetailDto;
import com.example.enjoy.entity.Track;
import com.example.enjoy.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracks")
public class TrackController {

    private final TrackService trackService;

//    /**
//     * 특정 트랙의 상세 정보를 조회하는 API
//     * @param trackId 조회할 트랙의 ID
//     * @return TrackDetailDto - 트랙의 상세 정보
//     */
//    @GetMapping("/{trackId}")
//    public TrackDetailDto getTrackDetailsById(@PathVariable Long trackId) {
//
//        // TODO: 추후 Spring Security 연동 후 실제 로그인한 학생 ID를 가져와야 함
//        String currentStudentId = "1";
//
//        // 5. 서비스의 메서드를 호출하여 결과를 받아온 후, 그대로 반환
//        return trackService.getTrackDetails(currentStudentId, trackId);
//    }

    @Operation(summary = "트랙 상세 정보 조회", description = "트랙의 이름으로 상세 정보를 조회합니다.")
    @GetMapping("/{trackId}")
    public ResponseEntity<TrackDetailDto> getTrackDetailsByName(@RequestParam String trackName) {
        TrackDetailDto trackDetailDto = trackService.getTrackDetailsByName(trackName);
        if (trackDetailDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(trackDetailDto);
    }

    @Operation(summary = "트랙 목록 조회", description = "모든 트랙의 목록을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<TrackDetailDto>> getAllTracks() {
        List<Track> tracks = trackService.getAllTracks();
        if (tracks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<TrackDetailDto> trackDetails = tracks.stream()
                .map(track -> new TrackDetailDto().from(track))
                .toList();
        return ResponseEntity.ok(trackDetails);
    }

}
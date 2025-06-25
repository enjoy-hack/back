package com.example.enjoy.controller;

import com.example.enjoy.dto.TrackDetailDto;
import com.example.enjoy.dto.UserTrackResponse;
import com.example.enjoy.entity.Track;
import com.example.enjoy.entity.UserTrack;
import com.example.enjoy.entity.user.User;
import com.example.enjoy.service.UserTrackService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-track")
public class UserTrackController {
    private final UserTrackService userTrackService;

    public UserTrackController(UserTrackService userTrackService) {
        this.userTrackService = userTrackService;
    }

    @Operation(summary = "사용자 관심 트랙 추가", description = "사용자가 관심 트랙을 추가합니다.")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserTrackResponse> addUserTrack(
            @RequestParam String studentId,
            @RequestParam String trackName) {
        UserTrack savedUserTrack = userTrackService.addUserTrack(studentId, trackName);
        UserTrackResponse response = UserTrackResponse.from(savedUserTrack);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 관심 트랙 조회", description = "사용자가 추가한 관심 트랙 목록을 조회합니다.")
    @GetMapping("/{studentId}")
    public ResponseEntity<List<TrackDetailDto>> getUserTracks(
            @PathVariable String studentId) {
        List<Track> tracks = userTrackService.getUserTracks(studentId);
        if (tracks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
       else{
            List<TrackDetailDto> trackDetails = tracks.stream()
                    .map(track -> new TrackDetailDto().from(track))
                    .toList();
            return ResponseEntity.ok(trackDetails);
        }
    }

    @Operation(summary = "사용자 관심 트랙 삭제", description = "사용자가 추가한 관심 트랙을 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> removeUserTrack(
            @RequestParam String studentId,
            @RequestParam String trackName) {
        userTrackService.removeUserTrack(studentId, trackName);
        return ResponseEntity.ok().build();
    }
}

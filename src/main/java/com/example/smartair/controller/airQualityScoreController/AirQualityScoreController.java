package com.example.smartair.controller.airQualityScoreController;

import com.example.smartair.dto.airQualityScoreDto.DeviceAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.PlaceAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.RoomAirQualityScoreDto;
import com.example.smartair.service.airQualityService.AirQualityQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/airquality/scores")
public class AirQualityScoreController {

    private final AirQualityQueryService airQualityQueryService;

    /**
     * 디바이스별 공기질 점수 기록을 조회합니다 (페이징 및 시간 필터링)
     * @param deviceId
     * @param startTime (선택) 조회 시작 시간
     * @param endTime (선택) 조회 종료 시간
     * @param pageable 페이징 및 정렬 정보
     * @return
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Page<DeviceAirQualityScoreDto>> getDeviceAirQualityScores(
            @PathVariable Long deviceId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            Pageable pageable
    ) {
        Page<DeviceAirQualityScoreDto> scorePage = airQualityQueryService.getDeviceAirQualityScores(
                deviceId, startTime, endTime, pageable
        );
        return ResponseEntity.ok(scorePage);
    }

    /**
     * 방 별 공기질 점수 기록을 조회합니다 (페이징)
     * @param roomId
     * @param startTime (선택) 조회 시작 시간
     * @param endTime (선택) 조회 종료 시간
     * @param pageable 페이징 및 정렬 정보
     * @return
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<Page<RoomAirQualityScoreDto>> getRoomAirQualityScores(
            @PathVariable Long roomId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            Pageable pageable
    ) {
        Page<RoomAirQualityScoreDto> scorePage = airQualityQueryService.getRoomAirQualityScores(roomId, startTime, endTime, pageable);
        return ResponseEntity.ok(scorePage);
    }

    /**
     * 공간별 공기질 점수 기록을 조회합니다 (페이징)
     * @param placeId
     * @param startTime (선택) 조회 시작 시간
     * @param endTime (선택) 조회 종료 시간
     * @param pageable 페이징 및 정렬 정보
     * @return
     */
    @GetMapping("/place/{placeId}")
    public ResponseEntity<Page<PlaceAirQualityScoreDto>> getPlaceAirQualityScores(
            @PathVariable Long placeId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            Pageable pageable
    ) {
        Page<PlaceAirQualityScoreDto> scorePage = airQualityQueryService.getPlaceAirQualityScores(placeId, startTime, endTime, pageable);
        return ResponseEntity.ok(scorePage);
    }

    /**
     * 디바이스의 가장 최신 공기질 점수 기록 한 건을 조회합니다
     * @param deviceId
     * @return
     */
    @GetMapping("/device")
    public ResponseEntity<DeviceAirQualityScoreDto> getLatestDeviceAirQualityScore(
            @PathVariable Long deviceId
    ) {
        DeviceAirQualityScoreDto score = airQualityQueryService.getLatestDeviceAirQualityScore(deviceId);
        return ResponseEntity.ok(score);
    }

    /**
     * 방의 가장 최신 공기질 점수 기록 한 건을 조회합니다
     * @param roomId
     * @return
     */
    @GetMapping("/room")
    public ResponseEntity<RoomAirQualityScoreDto> getLatestRoomAirQualityScore(
            @PathVariable Long roomId
    ){
        RoomAirQualityScoreDto score = airQualityQueryService.getLatestRoomAirQualityScore(roomId);
        return ResponseEntity.ok(score);
    }

    /**
     * 공간의 가장 최신 공기질 점수 기록 한 건을 조회합니다
     * @param placeId
     * @return
     */
    @GetMapping("/place")
    public ResponseEntity<PlaceAirQualityScoreDto> getLatestPlaceAirQualityScore(
            @PathVariable Long placeId
    ){
        PlaceAirQualityScoreDto score = airQualityQueryService.getLatestPlaceAirQualityScore(placeId);
        return ResponseEntity.ok(score);
    }
}

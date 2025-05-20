package com.example.smartair.controller.airQualityScoreController;

import com.example.smartair.dto.airQualityScoreDto.AverageScoreDto;
import com.example.smartair.dto.airQualityScoreDto.SensorAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.PlaceAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.RoomAirQualityScoreDto;
import com.example.smartair.service.airQualityService.AirQualityQueryService;
import com.example.smartair.service.airQualityService.AirQualityScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scores")
public class AirQualityScoreController implements AirQualityScoreControllerDocs{

    private final AirQualityQueryService airQualityQueryService; //공기질 점수 조회

    @GetMapping("/sensor/{serialNumber}/average")
    public ResponseEntity<AverageScoreDto> getSensorAverageScore(
            @PathVariable String serialNumber,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        AverageScoreDto averageScore = airQualityQueryService.getSensorAverageScore(serialNumber, startTime, endTime);
        return ResponseEntity.ok(averageScore);
    }

    @GetMapping("/room/{roomId}/average")
    public ResponseEntity<AverageScoreDto> getRoomAverageScore(
            @PathVariable Long roomId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime
    ) {
        AverageScoreDto averageScore = airQualityQueryService.getRoomAverageScore(roomId, startTime, endTime);
        return ResponseEntity.ok(averageScore);
    }


    @GetMapping("/sensor/{serialNumber}")
    public ResponseEntity<Page<SensorAirQualityScoreDto>> getSensorAirQualityScores(
            @PathVariable String serialNumber,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            Pageable pageable
    ) {
        Page<SensorAirQualityScoreDto> scorePage = airQualityQueryService.getSensorAirQualityScores(
                serialNumber, startTime, endTime, pageable
        );
        return ResponseEntity.ok(scorePage);
    }

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

//    @GetMapping("/place/{placeId}")
//    public ResponseEntity<Page<PlaceAirQualityScoreDto>> getPlaceAirQualityScores(
//            @PathVariable Long placeId,
//            @RequestParam(required = false) LocalDateTime startTime,
//            @RequestParam(required = false) LocalDateTime endTime,
//            Pageable pageable
//    ) {
//        Page<PlaceAirQualityScoreDto> scorePage = airQualityQueryService.getPlaceAirQualityScores(placeId, startTime, endTime, pageable);
//        return ResponseEntity.ok(scorePage);
//    }

    @GetMapping("/sensor/{serialNumber}/latest")
    public ResponseEntity<SensorAirQualityScoreDto> getLatestSensorAirQualityScore(
            @RequestParam String serialNumber
    ) {
        SensorAirQualityScoreDto score = airQualityQueryService.getLatestSensorAirQualityScore(serialNumber);
        return ResponseEntity.ok(score);
    }

    @GetMapping("/room")
    public ResponseEntity<RoomAirQualityScoreDto> getLatestRoomAirQualityScore(
            @PathVariable Long roomId
    ){
        RoomAirQualityScoreDto score = airQualityQueryService.getLatestRoomAirQualityScore(roomId);
        return ResponseEntity.ok(score);
    }

//    /**
//     * 공간의 가장 최신 공기질 점수 기록 한 건을 조회합니다
//     * @param placeId
//     * @return
//     */
//    @GetMapping("/place")
//    public ResponseEntity<PlaceAirQualityScoreDto> getLatestPlaceAirQualityScore(
//            @PathVariable Long placeId
//    ){
//        PlaceAirQualityScoreDto score = airQualityQueryService.getLatestPlaceAirQualityScore(placeId);
//        return ResponseEntity.ok(score);
//    }

}

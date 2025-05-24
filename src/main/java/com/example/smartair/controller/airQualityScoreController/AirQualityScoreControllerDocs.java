package com.example.smartair.controller.airQualityScoreController;

import com.example.smartair.dto.airQualityScoreDto.AverageScoreDto;
import com.example.smartair.dto.airQualityScoreDto.PlaceAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.RoomAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.SensorAirQualityScoreDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Tag(name = "AirQuality Score API", description = "센서, 방, 공간의 공기질 점수 조회 API")
public interface AirQualityScoreControllerDocs {

    @Operation(summary = "센서 평균 점수 조회", description = "특정 기간 동안의 센서 평균 공기질 점수를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음")
    })
    ResponseEntity<AverageScoreDto> getSensorAverageScore(
            @Parameter(description = "센서 일련번호", required = true) String serialNumber,
            @Parameter(description = "조회 시작 시간 (기본값: 24시간 전)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간 (기본값: 현재 시간)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime
    );

    @Operation(summary = "방 평균 점수 조회", description = "특정 기간 동안의 방 평균 공기질 점수를 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<AverageScoreDto> getRoomAverageScore(
            @Parameter(description = "방 ID", required = true) Long roomId,
            @Parameter(description = "조회 시작 시간 (기본값: 24시간 전)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간 (기본값: 현재 시간)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime
    );

    @Operation(summary = "센서 공기질 점수 조회", description = "특정 센서의 시간대별 공기질 점수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음")
    })
    ResponseEntity<Page<SensorAirQualityScoreDto>> getSensorAirQualityScores(
            @Parameter(description = "센서 일련번호", required = true) String serialNumber,
            @Parameter(description = "조회 시작 시간 (기본값: 24시간 전)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간 (기본값: 현재 시간)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "페이지네이션 정보 (예 : \"page\": 0, \"size\": 10, \"sort\": \"id,desc\")") Pageable pageable
    );

    @Operation(summary = "방 공기질 점수 조회", description = "특정 방의 시간대별 공기질 점수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<Page<RoomAirQualityScoreDto>> getRoomAirQualityScores(
            @Parameter(description = "방 ID", required = true) Long roomId,
            @Parameter(description = "조회 시작 시간 (기본값: 24시간 전)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간 (기본값: 현재 시간)", example = "2025-05-08T15:45:00")
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "페이지네이션 정보 (예 : \"page\": 0, \"size\": 10, \"sort\": \"id,desc\")") Pageable pageable
    );

    @Operation(summary = "센서 최신 공기질 점수 조회", description = "특정 센서의 가장 최근 공기질 점수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음")
    })
    ResponseEntity<SensorAirQualityScoreDto> getLatestSensorAirQualityScore(
            @Parameter(description = "센서 일련번호", required = true) String serialNumber
    );

    @Operation(summary = "방 최신 공기질 점수 조회", description = "특정 방의 가장 최근 공기질 점수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음")
    })
    ResponseEntity<RoomAirQualityScoreDto> getLatestRoomAirQualityScore(
            @Parameter(description = "방 ID", required = true) Long roomId
    );
}

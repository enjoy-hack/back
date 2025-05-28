package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.dto.airQualityDataDto.AirQualityDataResponse;
import com.example.smartair.dto.airQualityDataDto.HourlySensorAirQualitySnapshotResponse;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.login.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "AirQuality Snapshot API", description = "시간별 공기질 스냅샷 조회 API")
public interface AirQualitySnapshotControllerDocs {

    @Operation(summary = "특정 센서의 시간별 스냅샷 조회", description = "특정 센서의 지정된 시간 범위(YYYY-MM-DDTHH:MM:SS 형식)에 해당하는 시간별 대기질 스냅샷 리스트를 조회합니다."
            + " 시작 시간과 종료 시간을 지정하지 않으면, 기본적으로 현재 시간 기준으로 24시간 전부터 현재까지의 스냅샷을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "스냅샷 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HourlySensorAirQualitySnapshotResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "센서 또는 해당 시간 범위의 스냅샷을 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<HourlySensorAirQualitySnapshotResponse>> getHourlySnapshots(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "센서 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 시간 (YYYY-MM-DDTHH:MM:SS 형식)",
                    required = false, example = "2023-10-28T14:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간 (YYYY-MM-DDTHH:MM:SS 형식)",
                    required = false, example = "2023-10-28T18:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime);



    @Operation(summary = "특정 센서의 가장 최신의 대기질 데이터 조회",
            description = "특정 센서의 가장 최신의 대기질 데이터를 조회합니다. ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대기질 데이터 조회 성공",
                            content = @Content(schema = @Schema(implementation = AirQualityDataResponse.class))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<AirQualityDataResponse> getLatestSensorAirQualityData(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "센서 일련번호", required = true, example = "1") @PathVariable String serialNumber);

    @Operation(
            summary = "특정 센서의 시간별 스냅샷 수동 생성",
            description = "지정된 센서에 대해 특정 시간의 시간별 공기질 스냅샷을 수동으로 생성합니다.\n\n" +
                    "- 스냅샷 시간을 지정하지 않으면 현재 시간을 기준으로 생성합니다.\n" +
                    "- 해당 시간의 원시 데이터를 기반으로 평균값과 평균점수를 계산합니다.\n" +
                    "- 이미 스냅샷이 존재하는 경우 새로 계산하여 갱신합니다.\n" +
                    "- 지정된 시간에 데이터가 없는 경우 404 에러를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "스냅샷 생성 성공",
                            content = @Content(schema = @Schema(implementation = HourlySensorAirQualitySnapshotResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "센서를 찾을 수 없거나 해당 시간에 데이터가 없음",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    ResponseEntity<HourlySensorAirQualitySnapshotResponse> createHourlySnapshotForSensor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "센서 일련번호", required = true, example = "1") @PathVariable String serialNumber,
            @Parameter(description = "스냅샷 생성 기준 시간 (YYYY-MM-DDTHH:MM:SS 형식, 미입력시 현재 시간 사용)",
                    required = false, example = "2023-10-28T14:00:00")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime snapshotTime);

    /**
     * 특정 센서의 지정된 날짜 범위에 해당하는 모든 원시 대기질 데이터를 조회합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @param serialNumber 센서 일련번호
     * @param startDate 조회 시작 날짜 (YYYY-MM-DD 형식)
     * @param endDate 조회 종료 날짜 (YYYY-MM-DD 형식)
     * @return 해당 기간의 센서 대기질 데이터 목록
     */
    @Operation(summary = "특정 센서의 원시 대기질 데이터 목록 조회",
            description = "특정 센서의 지정된 날짜 범위(YYYY-MM-DD 형식)에 해당하는 모든 원시 대기질 데이터를 조회합니다."
            + " 시작 날짜와 종료 날짜를 지정하지 않으면, 기본적으로 현재 날짜 기준으로 24시간 전부터 현재까지의 데이터를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "대기질 데이터 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AirQualityDataResponse.class)))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<AirQualityDataResponse>> getAirQualityDataByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "센서 일련번호", required = true) @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = false, example = "2023-10-28")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = false, example = "2023-10-29")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );


} 
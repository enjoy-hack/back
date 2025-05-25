package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.dto.airQualityDataDto.AirQualityDataResponse;
import com.example.smartair.dto.airQualityDataDto.HourlySensorAirQualitySnapshotResponse;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.login.CustomUserDetails;
import com.example.smartair.service.airQualityService.snapshot.SnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
@Tag(name = "AirQuality Snapshot API", description = "시간별 대기질 스냅샷 조회 API")
public class AirQualitySnapshotController implements AirQualitySnapshotControllerDocs {

    private final SnapshotService snapshotService;

    @Override
    @GetMapping("/{serialNumber}/{startTime}/{endTime}")
    @Operation(summary = "특정 센서의 시간별 스냅샷 조회", description = "특정 센서의 지정된 시간 범위(YYYY-MM-DDTHH:MM:SS 형식)에 해당하는 시간별 대기질 스냅샷 리스트를 조회합니다.")
    public ResponseEntity <List<HourlySensorAirQualitySnapshotResponse>> getHourlySnapshots(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "센서 일련번호", required = true) @PathVariable String serialNumber,
            @Parameter(description = "조회 시작 시간 (YYYY-MM-DDTHH:MM:SS 형식)", required = true, example = "2023-10-28T14:00:00")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "조회 종료 시간 (YYYY-MM-DDTHH:MM:SS 형식)", required = true, example = "2023-10-28T18:00:00")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // snapshotHour의 분, 초, 나노초를 0으로 설정하여 정시 기준으로 만듭니다.
        List<HourlySensorAirQualitySnapshot> snapshot = snapshotService.getHourlySnapshots(serialNumber, startTime, endTime);
        List<HourlySensorAirQualitySnapshotResponse> responses = snapshot.stream()
                .map(HourlySensorAirQualitySnapshotResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(responses);
    }

    @Override
    @GetMapping("/latest/{serialNumber}")
    @Operation(summary = "특정 센서의 최신 대기질 데이터 조회", description = "특정 센서의 가장 최신의 대기질 데이터를 조회합니다.")
    public ResponseEntity<AirQualityDataResponse> getLatestSensorAirQualityData(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "센서 일련번호", required = true) @PathVariable String serialNumber) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SensorAirQualityData sensorAirQualityData = snapshotService.getLatestAirQualityData(serialNumber);
        return ResponseEntity.ok(AirQualityDataResponse.from(sensorAirQualityData));
    }
}

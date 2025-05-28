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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AirQuality Snapshot API", description = "시간별 대기질 스냅샷 조회 API")
public class AirQualitySnapshotController implements AirQualitySnapshotControllerDocs {

    private final SnapshotService snapshotService;

    @Override
    @GetMapping("/{serialNumber}")
    public ResponseEntity <List<HourlySensorAirQualitySnapshotResponse>> getHourlySnapshots(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String serialNumber,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetStartTime = startTime != null ? startTime : now.minusDays(1);
        LocalDateTime targetEndTime = endTime != null ? endTime : now;

        // snapshotHour의 분, 초, 나노초를 0으로 설정하여 정시 기준으로 만듭니다.
        List<HourlySensorAirQualitySnapshot> snapshot = snapshotService.getHourlySnapshots(serialNumber, targetStartTime, targetEndTime);
        List<HourlySensorAirQualitySnapshotResponse> responses = snapshot.stream()
                .map(HourlySensorAirQualitySnapshotResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(responses);
    }

    @Override
    @GetMapping("/latest/{serialNumber}")
    public ResponseEntity<AirQualityDataResponse> getLatestSensorAirQualityData(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String serialNumber) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SensorAirQualityData sensorAirQualityData = snapshotService.getLatestAirQualityData(serialNumber);
        return ResponseEntity.ok(AirQualityDataResponse.from(sensorAirQualityData));
    }

    @Override
    @PostMapping("/hourly/create/{serialNumber}")
    public ResponseEntity<HourlySensorAirQualitySnapshotResponse> createHourlySnapshotForSensor(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String serialNumber,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime snapshotTime) {

        // 권한 확인
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // 시간이 지정되지 않은 경우 현재 시간 사용
            LocalDateTime targetTime = snapshotTime != null ? snapshotTime : LocalDateTime.now();

            // 특정 센서에 대한 스냅샷 생성 서비스 호출
            HourlySensorAirQualitySnapshot snapshot = snapshotService.createHourlySnapshotForSensor(serialNumber, targetTime);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(HourlySensorAirQualitySnapshotResponse.from(snapshot));
        } catch (Exception e) {
            log.error("센서 {} 스냅샷 생성 중 오류 발생: {}", serialNumber, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @GetMapping("/raw/{serialNumber}")
    public ResponseEntity<List<AirQualityDataResponse>> getAirQualityDataByDateRange(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String serialNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LocalDate now = LocalDate.now();
        LocalDate targetStartTime = startDate != null ? startDate : now.minusDays(1);
        LocalDate targetEndTime = endDate != null ? endDate : now;

        // 시간 단위로 변환
        LocalDateTime startTime = targetStartTime.atStartOfDay();
        LocalDateTime endTime = targetEndTime.atTime(23, 59, 59);

        List<SensorAirQualityData> airQualityDataList = snapshotService.getAirQualityDataByDateRange(serialNumber, startTime, endTime);
        List<AirQualityDataResponse> responseList = airQualityDataList.stream()
                .map(AirQualityDataResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseList);
    }

}

package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.service.airQualityService.snapshot.SnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/snapshots")
@RequiredArgsConstructor
@Tag(name = "AirQuality Snapshot API", description = "시간별 공기질 스냅샷 조회 API")
public class AirQualitySnapshotController implements AirQualitySnapshotControllerDocs {

    private final SnapshotService snapshotService;

    @Override
    @GetMapping("/{sensorId}/{snapshotHour}")
    @Operation(summary = "특정 센서의 시간별 스냅샷 조회", description = "특정 센서의 지정된 시간(YYYY-MM-DDTHH:MM:SS 형식)에 해당하는 시간별 공기질 스냅샷 정보를 조회합니다.")
    public ResponseEntity<HourlySensorAirQualitySnapshot> getHourlySnapshot(
            @Parameter(description = "센서 ID", required = true) @PathVariable Long sensorId,
            @Parameter(description = "조회할 스냅샷 시간 (YYYY-MM-DDTHH:00:00 형식, 분/초는 0으로 처리됨)", required = true, example = "2023-10-28T14:00:00")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime snapshotHour) {

        // snapshotHour의 분, 초, 나노초를 0으로 설정하여 정시 기준으로 만듭니다.
        LocalDateTime normalizedSnapshotHour = snapshotHour.withMinute(0).withSecond(0).withNano(0);
        HourlySensorAirQualitySnapshot snapshot = snapshotService.getHourlySnapshot(sensorId, normalizedSnapshotHour);
        return ResponseEntity.ok(snapshot);
    }

    @Override
    @PostMapping("/generate/{snapshotHour}")
    @Operation(summary = "전체 센서의 시간별 공기질 스냅샷 생성", description = "모든 센서의 지정된 시간에 해당하는 시간별 공기질 스냅샷 정보를 생성합니다.")
    public ResponseEntity<HourlySensorAirQualitySnapshot> createHourlySnapshot(
            @Parameter(description = "센서 ID", required = true) @PathVariable Long sensorId,
            @Parameter(description = "생성할 스냅샷 시간 (YYYY-MM-DDTHH:00:00 형식, 분/초는 0으로 처리됨)", required = true, example = "2023-10-28T14:00:00")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime snapshotHour) {
        LocalDateTime normalizedHour = snapshotHour.truncatedTo(ChronoUnit.HOURS);
        snapshotService.createHourlySnapshot(normalizedHour);
        return ResponseEntity.ok().build();
    }
}

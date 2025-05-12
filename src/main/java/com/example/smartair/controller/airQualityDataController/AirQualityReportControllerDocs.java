package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.report.WeeklySensorAirQualityReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "AirQuality Report API", description = "일별 및 주간 공기질 리포트 조회 API")
public interface AirQualityReportControllerDocs {

    @Operation(summary = "특정 날짜의 일별 공기질 리포트 조회",
            description = "특정 센서의 지정된 날짜에 대한 일별 공기질 리포트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일별 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = DailySensorAirQualityReport.class))),
                    @ApiResponse(responseCode = "404", description = "센서 또는 해당 날짜의 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<DailySensorAirQualityReport> getDailyReport(
            @Parameter(description = "센서 ID", required = true, example = "1") Long sensorId,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-28")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @Operation(summary = "특정 기간의 일별 공기질 리포트 목록 조회",
            description = "특정 센서의 지정된 시작일과 종료일 사이의 모든 일별 공기질 리포트 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일별 리포트 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DailySensorAirQualityReport.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위 (시작일이 종료일보다 늦음)",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<DailySensorAirQualityReport>> getDailyReportsForPeriod(
            @Parameter(description = "센서 ID", required = true, example = "1") Long sensorId,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @Operation(summary = "특정 연도 및 주차의 주간 공기질 리포트 조회",
            description = "특정 센서의 지정된 연도와 주차(ISO 8601 기준)에 대한 주간 공기질 리포트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = WeeklySensorAirQualityReport.class))),
                    @ApiResponse(responseCode = "404", description = "센서 또는 해당 연도/주차의 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<WeeklySensorAirQualityReport> getWeeklyReport(
            @Parameter(description = "센서 ID", required = true, example = "1") Long sensorId,
            @Parameter(description = "조회할 연도 (YYYY 형식)", required = true, example = "2023") Integer year,
            @Parameter(description = "조회할 주차 (1-53 사이의 숫자, ISO 8601 기준)", required = true, example = "43") Integer weekOfYear);

    @Operation(summary = "특정 기간의 주간 공기질 리포트 목록 조회",
            description = "특정 센서의 지정된 시작일과 종료일 사이에 시작하는 모든 주간 공기질 리포트 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주간 리포트 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = WeeklySensorAirQualityReport.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위 (시작일이 종료일보다 늦음)",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<WeeklySensorAirQualityReport>> getWeeklyReportsForPeriod(
            @Parameter(description = "센서 ID", required = true, example = "1") Long sensorId,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @Operation(summary = "특정 ID의 일별 리포트 삭제",
            description = "지정된 리포트 ID에 해당하는 일별 공기질 리포트를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
                    @ApiResponse(responseCode = "404", description = "삭제할 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Void> deleteDailyReport(
            @Parameter(description = "삭제할 일별 리포트의 ID", required = true, example = "100") Long reportId);

    @Operation(summary = "특정 센서의 모든 일별 리포트 삭제",
            description = "지정된 센서 ID와 관련된 모든 일별 공기질 리포트를 삭제하고, 삭제된 리포트의 수를 반환합니다. " ,
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공 및 삭제된 리포트 수 반환",
                            content = @Content(schema = @Schema(type = "integer", format = "int32", example = "5"))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음 (삭제할 리포트가 없는 경우도 0을 반환)",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Integer> deleteDailyReportsByDeviceId(
            @Parameter(description = "모든 일별 리포트를 삭제할 센서의 ID", required = true, example = "1") Long deviceId);

    @Operation(summary = "생성된 지 N일이 지난 오래된 일별 보고서 삭제",
            description = "생성된 지 N일이 지난 오래된 일별 보고서를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
                    @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Integer> deleteOldDailyReports(
            @Parameter(description = "삭제할 일별 리포트의 ID", required = true, example = "100") Integer days);
} 
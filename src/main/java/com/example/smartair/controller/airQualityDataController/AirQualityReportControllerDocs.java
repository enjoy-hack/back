package com.example.smartair.controller.airQualityDataController;

import com.example.smartair.dto.airQualityDataDto.AnomalyReportDto;
import com.example.smartair.dto.airQualityDataDto.AnomalyReportResponseDto;
import com.example.smartair.dto.airQualityDataDto.dailyReportDto.DailyReportResponseDto;
import com.example.smartair.dto.airQualityDataDto.weeklyReportDto.WeeklyReportResponseDto;
import com.example.smartair.entity.airData.report.AnomalyReport;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "AirQuality Report API", description = "일별 및 주간 공기질 리포트 조회 API")
public interface AirQualityReportControllerDocs {

    @Operation(summary = "특정 날짜의 일별 공기질 리포트 조회",
            description = "특정 센서의 지정된 날짜에 대한 일별 공기질 리포트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일별 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = DailyReportResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "센서 또는 해당 날짜의 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<DailyReportResponseDto> getDailyReport(
            @Parameter(description = "센서 일련번호", required = true, example = "1") String serialNumber,
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-28")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);

    @Operation(summary = "특정 기간의 일별 공기질 리포트 목록 조회",
            description = "특정 센서의 지정된 시작일과 종료일 사이의 모든 일별 공기질 리포트 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "일별 리포트 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DailyReportResponseDto.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위 (시작일이 종료일보다 늦음)",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<DailyReportResponseDto>> getDailyReportsForPeriod(
            @Parameter(description = "센서 일련번호", required = true, example = "1") String serialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);

    @Operation(summary = "특정 연도 및 주차의 주간 공기질 리포트 조회",
            description = "특정 센서의 지정된 연도와 주차(ISO 8601 기준)에 대한 주간 공기질 리포트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주간 리포트 조회 성공",
                            content = @Content(schema = @Schema(implementation = WeeklyReportResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "센서 또는 해당 연도/주차의 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<WeeklyReportResponseDto> getWeeklyReport(
            @Parameter(description = "센서 일련번호", required = true, example = "1") String serialNumber,
            @Parameter(description = "조회할 연도 (YYYY 형식)", required = true, example = "2023") Integer year,
            @Parameter(description = "조회할 주차 (1-53 사이의 숫자, ISO 8601 기준)", required = true, example = "43") Integer weekOfYear);

    @Operation(summary = "특정 기간의 주간 공기질 리포트 목록 조회",
            description = "특정 센서의 지정된 시작일과 종료일 사이에 시작하는 모든 주간 공기질 리포트 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주간 리포트 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = WeeklyReportResponseDto.class)))),
                    @ApiResponse(responseCode = "400", description = "잘못된 날짜 범위 (시작일이 종료일보다 늦음)",
                            content = @Content(schema = @Schema(hidden = true))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<WeeklyReportResponseDto>> getWeeklyReportsForPeriod(
            @Parameter(description = "센서 일련번호", required = true, example = "1") String serialNumber,
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
            description = "지정된 센서 일련번호와 관련된 모든 일별 공기질 리포트를 삭제하고, 삭제된 리포트의 수를 반환합니다. " ,
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공 및 삭제된 리포트 수 반환",
                            content = @Content(schema = @Schema(type = "integer", format = "int32", example = "5"))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음 (삭제할 리포트가 없는 경우도 0을 반환)",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Integer> deleteDailyReportsByDeviceId(
            @Parameter(description = "모든 일별 리포트를 삭제할 센서의 일련번호", required = true, example = "1") String serialNumber);

    @Operation(summary = "생성된 지 N일이 지난 오래된 일별 보고서 삭제",
            description = "생성된 지 N일이 지난 오래된 일별 보고서를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
                    @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Integer> deleteOldDailyReports(
            @Parameter(description = "삭제할 일별 리포트의 ID", required = true, example = "100") Integer days);

    @Operation(summary = "특정 ID의 주간 리포트 삭제",
            description = "지정된 리포트 ID에 해당하는 주간 공기질 리포트를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
                    @ApiResponse(responseCode = "404", description = "삭제할 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Void> deleteWeeklyReport(
            @Parameter(description = "삭제할 주간 리포트의 ID", required = true, example = "100") Long reportId);

    @Operation(summary = "생성된 지 N주가 지난 오래된 주간 보고서 삭제",
            description = "생성된 지 N주가 지난 오래된 주간 보고서를 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
                    @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Integer> deleteOldWeeklyReports(
            @Parameter(description = "삭제할 주간 리포트의 ID", required = true, example = "100") Integer weeks);

    @Operation(summary = "특정 센서의 모든 주간 리포트 삭제",
            description = "지정된 센서 일련번호와 관련된 모든 주간 공기질 리포트를 삭제하고, 삭제된 리포트의 수를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공 및 삭제된 리포트 수 반환",
                            content = @Content(schema = @Schema(type = "integer", format = "int32", example = "5"))),
                    @ApiResponse(responseCode = "404", description = "센서를 찾을 수 없음 (삭제할 리포트가 없는 경우도 0을 반환)",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<Integer> deleteWeeklyReportsByDeviceId(
            @Parameter(description = "모든 주간 리포트를 삭제할 센서의 일련번호", required = true, example = "1") String serialNumber);

    @Operation(
            summary = "이상치 리포트 생성",
            description = """
        ## 설명
        - 이 API는 **AI 또는 외부 시스템에서 감지한 이상치 데이터를 기반**으로 이상치 리포트를 생성합니다.
        - 내부적으로 다음 정보를 기반으로 리포트를 생성합니다:
          - 센서 일치 여부 (serialNumber)
          - 해당 시각의 시간별 측정값 (`HourlySensorAirQualitySnapshot`)
          - 해당 날짜의 일별 보고서 (`DailySensorAirQualityReport`)
        - 또한, 예측값과 실제 측정값을 비교해 자동으로 설명(`description`)도 생성됩니다.

        ## 필드 설명
        - `sensorSerialNumber` (Long): 센서 고유 번호 (등록된 센서여야 함)
        - `anomalyTimestamp` (String, 예: "2023-10-10T10:00:00"): 이상치 발생 시각 (시간 단위까지 포함)
        - `pollutant` (String): 측정 항목 (예: "PM10", "TVOC", "CO2")
        - `pollutantValue` (Double): 실제 측정된 이상치 수치
        - `predictedValue` (Double): 예측된 수치

        ## 응답
        - 성공 시 Firebase 알림 전송 후 해당 메시지 ID 반환
        - 실패 시 상태 코드별 오류 반환
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이상치 리포트 생성 성공",
                            content = @Content(
                                    schema = @Schema(type = "string", example = "fcmMessageId12345")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터 (필수값 누락, 포맷 오류 등)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "센서 또는 관련 스냅샷/일일 리포트 없음",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    ResponseEntity<?> setAnomalyDailyReport(@RequestBody AnomalyReportDto anomalyReportDto);


    @Operation(summary = "특정 기간의 이상치 리포트 조회",
            description = "특정 센서의 지정된 기간 동안의 모든 이상치 리포트를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "이상치 리포트 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AnomalyReport.class)))),
                    @ApiResponse(responseCode = "404", description = "센서 또는 해당 기간의 리포트를 찾을 수 없음",
                            content = @Content(schema = @Schema(hidden = true)))
            })
    ResponseEntity<List<AnomalyReportResponseDto>> getAnomalyReports(
            @Parameter(description = "리포트를 조회할 센서의 일련번호", required = true, example = "1") @PathVariable String sensorSerialNumber,
            @Parameter(description = "조회 시작 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-01")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "조회 종료 날짜 (YYYY-MM-DD 형식)", required = true, example = "2023-10-31")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate);
}
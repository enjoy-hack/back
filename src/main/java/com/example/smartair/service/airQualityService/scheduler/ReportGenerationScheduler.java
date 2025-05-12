package com.example.smartair.service.airQualityService.scheduler;

import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.service.airQualityService.snapshot.SnapshotService;
import com.example.smartair.service.airQualityService.report.DailyReportService;
import com.example.smartair.service.airQualityService.report.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationScheduler {

    private final SensorRepository sensorRepository;
    private final SnapshotService snapshotService;
    private final DailyReportService dailyReportService;
    private final WeeklyReportService weeklyReportService;

    /**
     * 매시간 정각에 실행되어, 각 활성 센서에 대해 이전 시간의 시간별 공기질 스냅샷을 생성합니다.
     * 예: 14:00에 실행되면, 13:00의 스냅샷을 생성 (13:00:00 ~ 13:59:59 데이터 기반).
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 정각
    public void generateHourlySnapshots() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime snapshotHourBase = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);
        log.info("시간별 공기질 스냅샷 생성을 시작합니다. 기준 시간: {}", snapshotHourBase);

        try{
            snapshotService.createHourlySnapshot(snapshotHourBase);
            log.info("시간별 공기질 스냅샷 생성을 완료했습니다. 기준 시간: {}", snapshotHourBase);
        }catch(Exception e){
            log.error("시간별 공기질 스냅샷 생성 중 오류 발생: {}", e.getMessage(), e);
        }

    }

    /**
     * 매일 새벽 1시에 실행되어, 각 활성 센서에 대해 이전 날짜의 일별 공기질 리포트를 생성/업데이트합니다.
     */
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
    public void generateDailyReports() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("일별 공기질 리포트 생성을 시작합니다. 대상 날짜: {}", yesterday);

        List<Sensor> runningSensors = sensorRepository.findAllByRunningStatusIsTrue();
        if (runningSensors.isEmpty()) {
            log.info("활성 상태인 센서가 없어 일별 리포트 생성을 건너뛰었습니다.");
            return;
        }

        log.info("활성 센서 {}개에 대해 일별 리포트 생성을 시도합니다: {}", runningSensors.size());

        for (Sensor sensor : runningSensors) {
            try {
                log.debug("센서 ID {}에 대한 {} 날짜의 일별 리포트 생성을 시도합니다.", sensor.getId(), yesterday);
                dailyReportService.createOrUpdateDailyReport(sensor.getId(), yesterday);
                log.info("센서 ID {}에 대한 {} 날짜의 일별 리포트 생성 성공.", sensor.getId(), yesterday);
            } catch (Exception e) {
                log.error("센서 ID {}에 대한 {} 날짜의 일별 리포트 생성 중 오류 발생: {}",
                        sensor.getId(), yesterday, e.getMessage(), e);
            }
        }
        log.info("일별 공기질 리포트 생성을 완료했습니다.");
    }

    /**
     * 매주 월요일 새벽 2시에 실행되어, 각 활성 센서에 대해 이전 주의 주간 공기질 리포트를 생성/업데이트합니다.
     */
    @Scheduled(cron = "0 0 2 * * MON") // 매주 월요일 새벽 2시
    public void generateWeeklyReports() {
        LocalDate lastWeekDate = LocalDate.now().minusWeeks(1);
        int yearOfLastWeek = lastWeekDate.get(WeekFields.ISO.weekBasedYear());
        int weekOfLastWeek = lastWeekDate.get(WeekFields.ISO.weekOfWeekBasedYear());

        log.info("주간 공기질 리포트 생성을 시작합니다. 대상 연도: {}, 주차: {}", yearOfLastWeek, weekOfLastWeek);

        List<Sensor> runningSensors = sensorRepository.findAllByRunningStatusIsTrue();
        if (runningSensors.isEmpty()) {
            log.info("활성 상태인 센서가 없어 주간 리포트 생성을 건너뛰었습니다.");
            return;
        }

        log.info("활성 센서 {}개에 대해 주간 리포트 생성을 시도합니다.", runningSensors.size());

        for (Sensor sensor : runningSensors) {
            try {
                log.debug("센서 ID {}에 대한 {}년 {}주차 주간 리포트 생성을 시도합니다.",
                        sensor.getId(), yearOfLastWeek, weekOfLastWeek);
                weeklyReportService.createOrUpdateWeeklyReport(sensor.getId(), yearOfLastWeek, weekOfLastWeek);
                log.info("센서 ID {}에 대한 {}년 {}주차 주간 리포트 생성 성공.",
                        sensor.getId(), yearOfLastWeek, weekOfLastWeek);
            } catch (Exception e) {
                log.error("센서 ID {}에 대한 {}년 {}주차 주간 리포트 생성 중 오류 발생: {}",
                        sensor.getId(), yearOfLastWeek, weekOfLastWeek, e.getMessage(), e);
            }
        }
        log.info("주간 공기질 리포트 생성을 완료했습니다.");
    }
} 
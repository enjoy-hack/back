package com.example.smartair.entity.airData.report;

import com.example.smartair.entity.airScore.AirQualityGrade;
import com.example.smartair.entity.device.Device;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(
        name = "weekly_device_air_quality_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_device_year_week",
                        columnNames = {"device_id", "year_of_week", "week_of_year"}
                )
        }
)
public class WeeklyDeviceAirQualityReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "year_of_week", nullable = false)
    private Integer yearOfWeek; // 예: 2023

    @Column(name = "week_of_year", nullable = false)
    private Integer weekOfYear; // ISO 8601 주차 기준 (1 ~ 52/53)

    @Column(name = "start_date_of_week", nullable = false)
    private LocalDate startDateOfWeek; // 해당 주의 시작일

    @Column(name = "end_date_of_week", nullable = false)
    private LocalDate endDateOfWeek; // 해당 주의 종료일

    // 일일 보고서들과의 관계 설정
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "weekly_report_id")
    @OrderBy("reportDate ASC") // 날짜 순으로 정렬
    private List<DailyDeviceAirQualityReport> dailyReports = new ArrayList<>();

    // 주간 평균 데이터
    private Double weeklyAvgTemperature;
    private Double weeklyAvgHumidity;
    private Double weeklyAvgTvoc;
    private Double weeklyAvgEco2;
    private Double weeklyAvgPm25;

    // 주간 평균 점수
    private Double weeklyOverallScore;
    private Double weeklyPm25Score;
    private Double weeklyEco2Score;
    private Double weeklyTvocScore;

    // 주간 최고/최저 값
    private Double weeklyMaxTemperature;
    private Double weeklyMinTemperature;
    private Double weeklyMaxHumidity;
    private Double weeklyMinHumidity;
    private Double weeklyMaxPm25;
    private Integer weeklyMaxTvoc;
    private Integer weeklyMaxEco2;

    // 주간 유효 데이터 통계
    private Integer validDailyReportCount; // 유효한 일일 보고서 수 (0-7)
    private Integer totalDataPointCount; // 이 주간 보고서에 포함된 총 데이터 포인트 수

    // 주간 경향성 데이터 (선택적 - 온도, 습도 등의 변화 추세)
    private Double temperatureTrend; // 양수: 상승 추세, 음수: 하락 추세, 0: 변화 없음
    private Double humidityTrend;
    private Double pm25Trend;
    private Double eco2Trend;

    // 주간 품질 등급 (선택적)
    @Enumerated(EnumType.STRING)
    private AirQualityGrade airQualityGrade;
}
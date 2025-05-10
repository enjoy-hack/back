package com.example.smartair.entity.airData.report;

import com.example.smartair.entity.airData.snapshot.HourlyDeviceAirQualitySnapshot;
import com.example.smartair.entity.sensor.Sensor;
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
        name = "daily_device_air_quality_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_device_report_date",
                        columnNames = {"sensor_id", "report_date"}
                )
        }
)
public class DailyDeviceAirQualityReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate; // 리포트 날짜 (예: 2023-10-28)

    // 해당 날짜의 시간별 스냅샷 목록 (최대 24개)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "daily_report_id")
    @OrderBy("snapshotHour ASC") // 시간 순으로 정렬
    private List<HourlyDeviceAirQualitySnapshot> hourlySnapshots = new ArrayList<>();

    // 일일 평균 데이터
    private Double dailyAvgTemperature;
    private Double dailyAvgHumidity;
    private Double dailyAvgTvoc;
    private Double dailyAvgEco2;
    private Double dailyAvgPm25;

    // 일일 평균 점수
    private Double dailyOverallScore;
    private Double dailyPm25Score;
    private Double dailyEco2Score;
    private Double dailyTvocScore;

    // 최고/최저 값
    private Double dailyMaxTemperature;
    private Double dailyMinTemperature;
    private Double dailyMaxHumidity;
    private Double dailyMinHumidity;
    private Double dailyMaxPm25;
    private Integer dailyMaxTvoc;
    private Integer dailyMaxEco2;

    // 유효 데이터 포인트 수
    private Integer validDataPointCount; // 유효한 시간별 스냅샷 수 (0-24)
}
package com.example.smartair.entity.airData.report;

import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.entity.airData.snapshot.HourlyDeviceAirQualitySnapshot;
import com.example.smartair.entity.device.Device;
import com.example.smartair.util.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "anomaly_report")
public class AnomalyReport extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private LocalDateTime anomalyTimestamp; // 이상치 발생 시각

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Pollutant pollutant; // 예: "TVOC"

    private Double pollutantValue; // 이상치 값
    private String description; // 이상치에 대한 설명 또는 분석 결과

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hourly_snapshot_id") // 관련 시간별 스냅샷
    private HourlyDeviceAirQualitySnapshot relatedHourlySnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id") // 관련 일별 보고서
    private DailyDeviceAirQualityReport relatedDailyReport;
}

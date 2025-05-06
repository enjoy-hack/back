package com.example.smartair.dto.airQualityScoreDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder; // 빌더 추가 (선택적이지만 편리함)

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AverageScoreDto {
    private double overallScore;
    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;
}

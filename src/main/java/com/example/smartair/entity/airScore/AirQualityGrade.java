package com.example.smartair.entity.airScore;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum AirQualityGrade {
    EXCELLENT("매우 좋음", 401.0, 500.0),
    GOOD("좋음", 251.0, 400.0),
    MODERATE("보통", 101.0, 250.0),
    BAD("나쁨",51.0,100.0),
    VERY_BAD("매우 나쁨", 0.0 ,50.0)
    ;

    private final String label;
    private final double minScore;
    private final double maxScore;

    AirQualityGrade(String label, double minScore, double maxScore) {
        this.label = label;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static AirQualityGrade fromScore(double score) {
        return Arrays.stream(values())
                .filter(grade-> score>=grade.minScore && score<=grade.maxScore)
                .findFirst()
                .orElseThrow(()->new IllegalArgumentException("유효하지 않은 점수 :" + score));
    }
}

package com.example.smartair.service.airQualityService.calculator;

import com.example.smartair.domain.enums.Pollutant;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
public class AirQualityScoreCalculator implements AirQualityCalculator {

    // 구간별 기준 및 지수 정의 (농도 단위: PM=㎍/㎥, eCO2=ppm, TVOC=㎍/㎥)
    private static final double[][] PM10_BREAKPOINTS = {{0, 30}, {31, 80}, {81, 150}, {151, Double.MAX_VALUE}};
    private static final double[][] PM25_BREAKPOINTS = {{0, 15}, {16, 35}, {36, 75}, {76, Double.MAX_VALUE}};
    private static final double[][] ECO2_BREAKPOINTS = {{0, 500}, {501, 700}, {701, 1000}, {1001, Double.MAX_VALUE}};
    private static final double[][] TVOC_BREAKPOINTS = {{0, 400}, {401, 600}, {601, 1000}, {1001, Double.MAX_VALUE}};
    // 100점 체계로 변경된 지수 구간
    private static final double[][] AQI_BREAKPOINTS = {{0, 10}, {11, 20}, {21, 50}, {51, 100}};

    // AQI 100에 해당하는 실질적 최대 농도 정의 (마지막 구간 보간용)
    private static final double PM10_MAX_CONC_FOR_AQI_500 = 300.0;
    private static final double PM25_MAX_CONC_FOR_AQI_500 = 150.0;
    private static final double ECO2_MAX_CONC_FOR_AQI_500 = 2000.0;
    private static final double TVOC_MAX_CONC_FOR_AQI_500 = 2000.0;

    @Override
    public SensorAirQualityScore calculateScore(SensorAirQualityData airQualityData) {
        if (airQualityData == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_DATA, "공기질 데이터가 null입니다.");
        }

        FineParticlesData fineParticlesData = airQualityData.getFineParticlesData();

        double pm10Value = Optional.ofNullable(fineParticlesData)
                .map(FineParticlesData::getPm10_standard)
                .orElse(0.0);
        double pm25Value = Optional.ofNullable(fineParticlesData)
                .map(FineParticlesData::getPm25_standard)
                .orElse(0.0);

        // 개별 오염물질 점수 계산
        double pm10Score = calculatePollutantScore(Pollutant.PM10, pm10Value);
        double pm25Score = calculatePollutantScore(Pollutant.PM25, pm25Value);
        double eco2Score = calculatePollutantScore(Pollutant.ECO2, airQualityData.getEco2());
        double tvocScore = calculatePollutantScore(Pollutant.TVOC, airQualityData.getTvoc());

        // 점수 반전 (0-100 → 100-0)으로 변경
        pm10Score = 100 - pm10Score;
        pm25Score = 100 - pm25Score;
        eco2Score = 100 - eco2Score;
        tvocScore = 100 - tvocScore;

        // 통합 점수 계산 (개별 점수 중 최소값 - 이제 낮은 점수가 더 나쁜 것을 의미)
        double overallScore = Collections.min(Arrays.asList(pm10Score, pm25Score, eco2Score, tvocScore));

        // DeviceAirQualityScore 객체 생성 및 값 설정
        SensorAirQualityScore score = new SensorAirQualityScore();
        score.setSensorAirQualityData(airQualityData);
        score.setPm10Score(pm10Score);
        score.setPm25Score(pm25Score);
        score.setEco2Score(eco2Score);
        score.setTvocScore(tvocScore);
        score.setOverallScore(overallScore);

        return score;
    }

    @Override
    public double calculatePollutantScore(Pollutant pollutant, double concentration) {
        double[][] concentrationBreakpoints;
        double practicalMaxConc;

        switch (pollutant) {
            case PM10:
                concentrationBreakpoints = PM10_BREAKPOINTS;
                practicalMaxConc = PM10_MAX_CONC_FOR_AQI_500;
                break;
            case PM25:
                concentrationBreakpoints = PM25_BREAKPOINTS;
                practicalMaxConc = PM25_MAX_CONC_FOR_AQI_500;
                break;
            case ECO2:
                concentrationBreakpoints = ECO2_BREAKPOINTS;
                practicalMaxConc = ECO2_MAX_CONC_FOR_AQI_500;
                break;
            case TVOC:
                concentrationBreakpoints = TVOC_BREAKPOINTS;
                practicalMaxConc = TVOC_MAX_CONC_FOR_AQI_500;
                break;
            default: throw new CustomException(ErrorCode.UNKNOWN_POLLUTANT_TYPE);
        }

        // 음수 농도 예외 처리
        if (concentration < 0) {
            throw new CustomException(ErrorCode.INVALID_CONCENTRATION_RANGE, "concentration: " + concentration);
        }

        for (int i = 0; i < concentrationBreakpoints.length; i++) {
            double cLow = concentrationBreakpoints[i][0];
            double cHigh = concentrationBreakpoints[i][1];
            double iLow = AQI_BREAKPOINTS[i][0];
            double iHigh = AQI_BREAKPOINTS[i][1];

            boolean isLastRange = (i == concentrationBreakpoints.length - 1);

            // 마지막 무한 구간의 실제 cHigh를 실질적 최대 농도로 설정
            if (isLastRange && cHigh == Double.MAX_VALUE) {
                cHigh = practicalMaxConc;
            }

            // 농도가 현재 구간에 속하는지 확인
            if ((concentration >= cLow && concentration <= cHigh) || (isLastRange && concentration >= cLow)) {
                // 농도가 실질적 최대 농도를 초과하면 최고 점수(100) 반환
                if (isLastRange && concentration > practicalMaxConc) {
                    return 100.0; // 최고 점수 100 (나쁨)
                }

                // 선형 보간법 적용 (0으로 나누는 경우 방지)
                if (cHigh - cLow == 0) {
                    return iLow;
                }

                double score = ((iHigh - iLow) / (cHigh - cLow)) * (concentration - cLow) + iLow;
                return Math.round(score); // 정수로 반올림
            }
        }

        // 이 부분은 이론적으로 도달하지 않아야 함
        if (concentration >= concentrationBreakpoints[concentrationBreakpoints.length - 1][0]) {
            return 100.0; // 최고 점수 100으로 변경
        }

        throw new CustomException(ErrorCode.CALCULATION_LOGIC_ERROR);
    }
}
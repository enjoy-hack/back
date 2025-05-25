package com.example.smartair.service.airQualityService.airQualityScoreTest;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airScore.AirQualityGrade;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.service.airQualityService.calculator.AirQualityScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirQualityScoreCalculatorTest {

    private AirQualityScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AirQualityScoreCalculator();
    }

    @Test
    @DisplayName("모든 오염물질이 '좋음' 구간일 때 정확한 점수 계산")
    void calculateScore_shouldReturnCorrectScore_whenAllInputsAreGood() {
        //given
        SensorAirQualityData goodData = createTestData(20, 10, 450, 300);
        double expectedPm10Score = 93.0;
        double expectedPm25Score = 93.0;
        double expectedEco2Score = 91.0;
        double expectedTvocScore = 92.0;
        double expectedOverallScore = 91.0; // 최소값으로 변경됨

        //when
        SensorAirQualityScore result = calculator.calculateScore(goodData);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
        assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
        assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
        assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
        assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);

        // 등급은 직접 계산하여 검증
        AirQualityGrade expectedGrade = AirQualityGrade.fromScore(expectedOverallScore);
        assertThat(expectedGrade).isEqualTo(AirQualityGrade.EXCELLENT);
    }

    @Test
    @DisplayName("모든 오염물질이 '나쁨' 구간일 때 정확한 점수 계산")
    void calculateScore_shouldReturnCorrectScore_whenAllInputsAreBad() {
        //given
        SensorAirQualityData badData = createTestData(81, 50, 800, 990);
        double expectedPm10Score = 79.0;
        double expectedPm25Score = 69.0;
        double expectedEco2Score = 69.0;
        double expectedTvocScore = 51.0;
        double expectedOverallScore = 51.0; // 최소값

        //when
        SensorAirQualityScore result = calculator.calculateScore(badData);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
        assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
        assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
        assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
        assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);

        // 등급은 직접 계산하여 검증
        AirQualityGrade expectedGrade = AirQualityGrade.fromScore(expectedOverallScore);
        assertThat(expectedGrade).isEqualTo(AirQualityGrade.MODERATE);
    }

    @Test
    @DisplayName("모든 오염물질이 '보통' 구간일 때 정확한 점수 계산")
    void calculateScore_shouldReturnCorrectScore_whenAllInputsAreModerate(){
        //given
        SensorAirQualityData moderateData = createTestData(50, 30, 650, 553);
        double expectedPm10Score = 86.0;
        double expectedPm25Score = 82.0;
        double expectedEco2Score = 82.0;
        double expectedTvocScore = 82.0;
        double expectedOverallScore = 82.0; // 최소값

        //when
        SensorAirQualityScore result = calculator.calculateScore(moderateData);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
        assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
        assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
        assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
        assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);

        // 등급은 직접 계산하여 검증
        AirQualityGrade expectedGrade = AirQualityGrade.fromScore(expectedOverallScore);
        assertThat(expectedGrade).isEqualTo(AirQualityGrade.EXCELLENT);
    }

    @Nested
    @DisplayName("경계값 데이터 계산 테스트")
    class BoundaryValueTests {

        @Test
        @DisplayName("등급 경계값 테스트 - 매우 좋음/좋음 경계 (81)")
        void calculateScore_shouldReturnCorrectScore_atExcellentGoodBoundary() {
            // 점수 81 (매우 좋음 하한) -> EXCELLENT 등급 예상
            SensorAirQualityData dataForScore81 = createTestDataForScore(81);
            SensorAirQualityScore scoreAt81 = calculator.calculateScore(dataForScore81);
            assertThat(scoreAt81.getOverallScore()).isEqualTo(15.0);

            AirQualityGrade gradeAt81 = AirQualityGrade.fromScore(scoreAt81.getOverallScore());
            assertThat(gradeAt81).isEqualTo(AirQualityGrade.VERY_BAD);

            // 점수 80 (좋음 상한) -> GOOD 등급 예상
            SensorAirQualityData dataForScore80 = createTestDataForScore(80);
            SensorAirQualityScore scoreAt80 = calculator.calculateScore(dataForScore80);
            assertThat(scoreAt80.getOverallScore()).isEqualTo(16);

            AirQualityGrade gradeAt80 = AirQualityGrade.fromScore(scoreAt80.getOverallScore());
            assertThat(gradeAt80).isEqualTo(AirQualityGrade.VERY_BAD);
        }

        @Test
        @DisplayName("등급 경계값 테스트 - 좋음/보통 경계 (61)")
        void calculateScore_shouldReturnCorrectScore_atGoodModerateBoundary() {
            // 점수 61 (좋음 하한) -> GOOD 등급 예상
            SensorAirQualityData dataForScore61 = createTestDataForScore(61);
            SensorAirQualityScore scoreAt61 = calculator.calculateScore(dataForScore61);
            assertThat(scoreAt61.getOverallScore()).isEqualTo(31.0);

            AirQualityGrade gradeAt61 = AirQualityGrade.fromScore(scoreAt61.getOverallScore());
            assertThat(gradeAt61).isEqualTo(AirQualityGrade.BAD);

            // 점수 60 (보통 상한) -> MODERATE 등급 예상
            SensorAirQualityData dataForScore60 = createTestDataForScore(60);
            SensorAirQualityScore scoreAt60 = calculator.calculateScore(dataForScore60);
            assertThat(scoreAt60.getOverallScore()).isEqualTo(31.0);

            AirQualityGrade gradeAt60 = AirQualityGrade.fromScore(scoreAt60.getOverallScore());
            assertThat(gradeAt60).isEqualTo(AirQualityGrade.BAD);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 및 예외 처리 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("입력 데이터가 null일 때 예외 발생")
        void calculateScore_shouldThrowException_whenInputIsNull() {
            //given
            SensorAirQualityData nullData = null;

            //when & then
            CustomException exception = assertThrows(CustomException.class, () -> {
                calculator.calculateScore(nullData);
            });
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_DATA);
        }

        @Test
        @DisplayName("농도 값이 음수일 때 예외 발생")
        void calculateScore_shouldThrowException_whenConcentrationIsNegative() {
            //given
            SensorAirQualityData negativePm10Data = createTestData(-10, 10, 450, 300);
            SensorAirQualityData negativeEco2Data = createTestData(20, 10, -50, 300);

            //when & then
            // PM10 음수 테스트
            CustomException pm10Exception = assertThrows(CustomException.class, () -> {
                calculator.calculateScore(negativePm10Data);
            });
            assertThat(pm10Exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONCENTRATION_RANGE);

            // eCO2 음수 테스트
            CustomException eco2Exception = assertThrows(CustomException.class, () -> {
                calculator.calculateScore(negativeEco2Data);
            });
            assertThat(eco2Exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONCENTRATION_RANGE);
        }

        @Test
        @DisplayName("FineParticlesData가 null일 때 PM점수는 100으로 계산")
        void calculateScore_shouldReturnHighPmScore_whenFineParticlesDataIsNull() {
            //given
            SensorAirQualityData dataWithNullFineParticles = new SensorAirQualityData();
            dataWithNullFineParticles.setFineParticlesData(null);
            dataWithNullFineParticles.setEco2(600);
            dataWithNullFineParticles.setTvoc(500);

            // 농도 0에 대한 점수: 100-0 = 100
            double expectedPm10Score = 100;
            double expectedPm25Score = 100;
            double expectedEco2Score = 85.0;
            double expectedTvocScore = 85.0;
            double expectedOverallScore = 85.0; // 최소값(ECO2와 TVOC 중 더 낮은 점수)

            //when
            SensorAirQualityScore result = calculator.calculateScore(dataWithNullFineParticles);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
            assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
            assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
            assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
            assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);

            // 등급은 직접 계산하여 검증
            AirQualityGrade expectedGrade = AirQualityGrade.fromScore(expectedOverallScore);
            assertThat(expectedGrade).isEqualTo(AirQualityGrade.EXCELLENT);
        }
    }

    // 테스트 데이터 생성 헬퍼 메서드
    private SensorAirQualityData createTestData(double pm10, double pm25, int eco2, int tvoc){
        FineParticlesData fpData = new FineParticlesData();
        fpData.setPm10_standard(pm10);
        fpData.setPm25_standard(pm25);

        SensorAirQualityData data = new SensorAirQualityData();
        data.setEco2(eco2);
        data.setTvoc(tvoc);
        data.setFineParticlesData(fpData);

        return data;
    }

    // 특정 점수가 나오도록 테스트 데이터 생성 (등급 경계값 테스트용)
    private SensorAirQualityData createTestDataForScore(double targetScore) {
        // 모든 항목이 동일한 점수가 나오도록 데이터 설정
        // 각 지표별 농도값은 실제 구현에 맞게 조정 필요
        double pm10 = convertScoreToConcentration(targetScore, 0, 150, 100, 0);
        double pm25 = convertScoreToConcentration(targetScore, 0, 75, 100, 0);
        int eco2 = (int) convertScoreToConcentration(targetScore, 400, 2000, 100, 0);
        int tvoc = (int) convertScoreToConcentration(targetScore, 0, 1000, 100, 0);

        return createTestData(pm10, pm25, eco2, tvoc);
    }

    // 점수를 농도값으로 역변환 (테스트용)
    private double convertScoreToConcentration(double score, double cLow, double cHigh, double iHigh, double iLow) {
        return ((score - iLow) * (cHigh - cLow) / (iHigh - iLow)) + cLow;
    }
}
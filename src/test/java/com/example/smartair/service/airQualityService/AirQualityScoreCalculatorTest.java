package com.example.smartair.service.airQualityService;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
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
        double expectedPm10Score = 33;
        double expectedPm25Score = 33;
        double expectedEco2Score = 45;
        double expectedTvocScore = 38;
        double expectedOverallScore = 45;

        //when
        SensorAirQualityScore result = calculator.calculateScore(goodData);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
        assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
        assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
        assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
        assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);

    }

    @Test
    @DisplayName("모든 오염물질이 '나쁨' 구간일 때 정확한 점수 계산")
    void calculateScore_shouldReturnCorrectScore_whenAllInputsAreBad() {
        //given
        SensorAirQualityData badData = createTestData(81, 50, 800, 990);
        double expectedPm10Score = 101;
        double expectedPm25Score = 154;
        double expectedEco2Score = 150;
        double expectedTvocScore = 246;
        double expectedOverallScore = 246;

        //when
        SensorAirQualityScore result = calculator.calculateScore(badData);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
        assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
        assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
        assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
        assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);
    }

    @Test
    @DisplayName("모든 오염물질이 '보통' 구간일 때 정확한 점수 계산")
    void calculateScore_shouldReturnCorrectScore_whenAllInputsAreModerate(){
        //given
        SensorAirQualityData moderateData = createTestData(50, 40, 650, 553);
        double expectedPm10Score = 70;
        double expectedPm25Score = 116;
        double expectedEco2Score = 88;
        double expectedTvocScore = 88;
        double expectedOverallScore = 116;

        //when
        SensorAirQualityScore result = calculator.calculateScore(moderateData);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
        assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
        assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
        assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
        assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);
    }

    @Nested
    @DisplayName("경계값 데이터 계산 테스트")
    class BoundaryValueTests {

        @Test
        @DisplayName("PM10 좋음-보통 경계값 (30, 31) 테스트")
        void calculateScore_shouldReturnCorrectScore_atPm10GoodModerateBoundary(){
            // PM10 = 30 (좋음 상한) -> 점수 50 예상
            SensorAirQualityData dataAt30 = createTestData(30, 10, 450, 300);
            SensorAirQualityScore scoreAt30 = calculator.calculateScore(dataAt30);
            assertThat(scoreAt30.getPm10Score()).isEqualTo(50);
            assertThat(scoreAt30.getOverallScore()).isEqualTo(50); // PM10이 최대값

            // PM10 = 31 (보통 하한) -> 점수 51 예상
            SensorAirQualityData dataAt31 = createTestData(31, 10, 450, 300);
            SensorAirQualityScore scoreAt31 = calculator.calculateScore(dataAt31);
            assertThat(scoreAt31.getPm10Score()).isEqualTo(51);
            assertThat(scoreAt31.getOverallScore()).isEqualTo(51); // PM10이 최대값
        }

        @Test
        @DisplayName("PM2.5 보통-나쁨 경계값 (35, 36) 테스트")
        void calculateScore_shouldReturnCorrectScore_atPm25ModerateBadBoundary(){
            // PM2.5 = 35 (보통 상한) -> 점수 100 예상
            SensorAirQualityData dataAt35 = createTestData(50, 35, 650, 553);
            SensorAirQualityScore scoreAt35 = calculator.calculateScore(dataAt35);
            assertThat(scoreAt35.getPm25Score()).isEqualTo(100);
            assertThat(scoreAt35.getOverallScore()).isEqualTo(100); // PM2.5가 최대값

            // PM2.5 = 36 (나쁨 하한) -> 점수 101 예상
            SensorAirQualityData dataAt36 = createTestData(50, 36, 650, 553);
            SensorAirQualityScore scoreAt36 = calculator.calculateScore(dataAt36);
            assertThat(scoreAt36.getPm25Score()).isEqualTo(101);
            assertThat(scoreAt36.getOverallScore()).isEqualTo(101); // PM2.5가 최대값
        }

        @Test
        @DisplayName("eCO2 나쁨-매우나쁨 경계값 (1000, 1001) 테스트")
        void calculateScore_shouldReturnCorrectScore_atEco2BadVeryBadBoundary(){
            // eCO2 = 1000 (나쁨 상한) -> 점수 250 예상
            SensorAirQualityData dataAt1000 = createTestData(81, 50, 1000, 990);
            SensorAirQualityScore scoreAt1000 = calculator.calculateScore(dataAt1000);
            assertThat(scoreAt1000.getEco2Score()).isEqualTo(250);
            assertThat(scoreAt1000.getOverallScore()).isEqualTo(250); // eCO2가 최대값

            // eCO2 = 1001 (매우나쁨 하한) -> 점수 251 예상 
            SensorAirQualityData dataAt1001 = createTestData(81, 50, 1001, 990);
            SensorAirQualityScore scoreAt1001 = calculator.calculateScore(dataAt1001);
            assertThat(scoreAt1001.getEco2Score()).isEqualTo(251); 
            assertThat(scoreAt1001.getOverallScore()).isEqualTo(251); // eCO2가 최대값
        }

        @Test
        @DisplayName("TVOC 0 농도 테스트")
        void calculateScore_shouldReturnZeroScore_whenTvocIsZero() {
            SensorAirQualityData dataWithZeroTvoc = createTestData(20, 10, 450, 0);
            SensorAirQualityScore score = calculator.calculateScore(dataWithZeroTvoc);
            assertThat(score.getTvocScore()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 및 예외 처리 테스트")
    class EdgeCaseTests{

        @Test
        @DisplayName("입력 데이터가 null일 때 예외 발생")
        void calculateScore_shouldThrowException_whenInputIsNull() {
            //given
            SensorAirQualityData nullData = null;

            //when & then
            CustomException exception = assertThrows(CustomException.class, ()->{
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
            CustomException pm10Exception = assertThrows(CustomException.class, ()->{
                calculator.calculateScore(negativePm10Data);
            });
            assertThat(pm10Exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONCENTRATION_RANGE);

            // eCO2 음수 테스트
            CustomException eco2Exception = assertThrows(CustomException.class, ()->{
                calculator.calculateScore(negativeEco2Data);
            });
            assertThat(eco2Exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CONCENTRATION_RANGE);
        }

        @Test
        @DisplayName("FineParticlesData가 null일 때 PM점수는 0으로 계산")
        void calculateScore_shouldReturnZeroPmScore_whenFineParticlesDataIsNull() {
            //given
            SensorAirQualityData dataWithNullFineParticles = new SensorAirQualityData();
            dataWithNullFineParticles.setFineParticlesData(null);
            dataWithNullFineParticles.setEco2(600);
            dataWithNullFineParticles.setTvoc(500);
            double expectedPm10Score = 0;
            double expectedPm25Score = 0;
            double expectedEco2Score = 75;
            double expectedTvocScore = 75;
            double expectedOverallScore = 75;

            //when
            SensorAirQualityScore result = calculator.calculateScore(dataWithNullFineParticles);

            //then
            assertThat(result).isNotNull();
            assertThat(result.getPm10Score()).isEqualTo(expectedPm10Score);
            assertThat(result.getPm25Score()).isEqualTo(expectedPm25Score);
            assertThat(result.getEco2Score()).isEqualTo(expectedEco2Score);
            assertThat(result.getTvocScore()).isEqualTo(expectedTvocScore);
            assertThat(result.getOverallScore()).isEqualTo(expectedOverallScore);
        }

    }


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




}
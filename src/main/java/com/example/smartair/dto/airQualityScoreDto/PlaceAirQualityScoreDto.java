package com.example.smartair.dto.airQualityScoreDto;

import com.example.smartair.entity.airScore.PlaceAirQualityScore;
import com.example.smartair.entity.place.Place;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PlaceAirQualityScoreDto {
    private LocalDateTime createdAt;
    private double overallScore;
    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;
    private Long placeId;
    private String placeName;

    public static PlaceAirQualityScoreDto fromEntity(PlaceAirQualityScore placeAirQualityScore) {
        if (placeAirQualityScore == null) {
            throw new CustomException(ErrorCode.PLACE_SCORE_NOT_FOUND);
        }

        if (placeAirQualityScore.getPlace() == null){
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }

        return PlaceAirQualityScoreDto.builder()
                .createdAt(placeAirQualityScore.getCreatedAt())
                .overallScore(placeAirQualityScore.getOverallScore())
                .pm10Score(placeAirQualityScore.getPm10Score())
                .pm25Score(placeAirQualityScore.getPm25Score())
                .eco2Score(placeAirQualityScore.getEco2Score())
                .tvocScore(placeAirQualityScore.getTvocScore())
                .placeId(placeAirQualityScore.getPlace().getId())
                .placeName(placeAirQualityScore.getPlace().getName())
                .build();
    }
}

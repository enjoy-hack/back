package com.example.smartair.dto.airQualityScoreDto;

import com.example.smartair.entity.airScore.RoomAirQualityScore;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RoomAirQualityScoreDto {
    private LocalDateTime createdAt;
    private double overallScore;
    private double pm10Score;
    private double pm25Score;
    private double eco2Score;
    private double tvocScore;
    private Long roomId;
    private String roomName;

    public static RoomAirQualityScoreDto fromEntity(RoomAirQualityScore roomAirQualityScore) {
        if (roomAirQualityScore == null) {
            throw new CustomException(ErrorCode.ROOM_SCORE_NOT_FOUND);
        }

        if (roomAirQualityScore.getRoom() == null){
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        }

        return RoomAirQualityScoreDto.builder()
                .createdAt(roomAirQualityScore.getCreatedAt())
                .overallScore(roomAirQualityScore.getOverallScore())
                .pm10Score(roomAirQualityScore.getPm10Score())
                .pm25Score(roomAirQualityScore.getPm25Score())
                .eco2Score(roomAirQualityScore.getEco2Score())
                .tvocScore(roomAirQualityScore.getTvocScore())
                .roomId(roomAirQualityScore.getRoom().getId())
                .roomName(roomAirQualityScore.getRoom().getName())
                .build();
    }
}

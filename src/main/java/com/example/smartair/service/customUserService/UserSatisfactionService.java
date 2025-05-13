package com.example.smartair.service.customUserService;

import com.example.smartair.dto.customUserDto.UserSatisfactionResponseDto;
import com.example.smartair.entity.airData.airQualityData.RoomAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.user.User;
import com.example.smartair.entity.user.UserSatisfaction;
import com.example.smartair.exception.CustomException;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.userSatisfactionRepository.UserSatisfactionRepository;
import com.example.smartair.service.airQualityService.AirQualityQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.smartair.exception.ErrorCode.*;

@Service
public class UserSatisfactionService {

    private final UserSatisfactionRepository userSatisfactionRepository;
    private final AirQualityQueryService airQualityQueryService;
    private final RoomAirQualityScoreRepository roomAirQualityScoreRepository;
    @Autowired
    public UserSatisfactionService(UserSatisfactionRepository userSatisfactionRepository,
                                   AirQualityQueryService airQualityQueryService,
                                   RoomAirQualityScoreRepository roomAirQualityScoreRepository){
        this.userSatisfactionRepository = userSatisfactionRepository;
        this.airQualityQueryService = airQualityQueryService;
        this.roomAirQualityScoreRepository = roomAirQualityScoreRepository;
    }
    //
    public void setUserSatisfaction(User user, Double satisfaction, Long roomId) throws Exception {
        //일단 가장 최근 roomAirQuality로 생성, 추후 현재 데이터로 변경 예정
        UserSatisfaction userSatisfaction = new UserSatisfaction();

        Optional<RoomAirQualityScore> optionalRoomAirQualityScore = roomAirQualityScoreRepository.findFirstByRoomIdOrderByCreatedAtDesc(roomId);
        if(optionalRoomAirQualityScore.isEmpty()){
            throw new Exception(new CustomException(ROOM_AIR_QUALITY_SCORE_IS_EMPTY));
        }
        RoomAirQualityScore roomAirQualityScore = optionalRoomAirQualityScore.get();
        //airData 해야함
        RoomAirQualityData roomAirQualityData = roomAirQualityScore.getRoomAirQualityData();

        userSatisfaction.setSatisfaction(satisfaction);
        userSatisfaction.setUserId(user.getId());
        userSatisfaction.setRoomAirQualityScore(roomAirQualityScore);
        userSatisfaction.setRoomAirQualityData(roomAirQualityData);

        userSatisfactionRepository.save(userSatisfaction);
    }

    //최대 7개의 데이터 반환
    public List<UserSatisfactionResponseDto> getUserSatisfaction(User user, Long roomId) throws Exception {

        List<UserSatisfaction> allByRoom = userSatisfactionRepository.findTop7ByRoomIdOrderByCreatedAtDesc(roomId);

        if (allByRoom == null || allByRoom.isEmpty()) {
            return Collections.emptyList();
        }

        return allByRoom.stream().map(us -> {
            RoomAirQualityScore score = us.getRoomAirQualityScore();
            RoomAirQualityData data = us.getRoomAirQualityData();
            return UserSatisfactionResponseDto.builder()
                    .id(us.getId())                                 // 사용자 만족도 id// 사용자 id
                    .satisfaction(us.getSatisfaction())             // 만족도 점수
                    .createdAt(us.getCreatedAt())                   // 생성 시간
                    .roomAirQualityScore(score.getOverallScore())
                    // 공기질 데이터 값들
                    .avgTemperature(data.getAvgTemperature())
                    .avgHumidity(data.getAvgHumidity())
                    .avgPressure(data.getAvgPressure())
                    .avgTvoc(data.getAvgTvoc())
                    .avgEco2(data.getAvgEco2())
                    .avgRawh2(data.getAvgRawh2())
                    .avgRawethanol(data.getAvgRawethanol())

                    .build();
        }).toList();
    }

    // 만족도 수정
    public void updateUserSatisfaction(User user, Long satisfactionId, Double newSatisfaction) throws Exception {
        Optional<UserSatisfaction> optional = userSatisfactionRepository.findById(satisfactionId);
        if (optional.isEmpty()) {
            throw new Exception(new CustomException(SATISFACTION_NOT_FOUND));
        }

        UserSatisfaction us = optional.get();

        if (!us.getUserId().equals(user.getId())) {
            throw new Exception(new CustomException(INVALID_REQUEST));
        }

        us.setSatisfaction(newSatisfaction);
        userSatisfactionRepository.save(us);
    }

    // 만족도 삭제
    public void deleteUserSatisfaction(User user, Long satisfactionId) throws Exception {
        Optional<UserSatisfaction> optional = userSatisfactionRepository.findById(satisfactionId);
        if (optional.isEmpty()) {
            throw  new Exception(new CustomException(SATISFACTION_NOT_FOUND));
        }

        UserSatisfaction us = optional.get();

        if (!us.getUserId().equals(user.getId())) {
            throw new Exception(new CustomException(INVALID_REQUEST));
        }

        userSatisfactionRepository.delete(us);
    }
}
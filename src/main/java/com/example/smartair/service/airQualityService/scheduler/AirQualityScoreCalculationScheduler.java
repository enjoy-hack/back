package com.example.smartair.service.airQualityService.scheduler;

import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.room.Room;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.service.airQualityService.AirQualityScoreService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Setter
@RequiredArgsConstructor
@Slf4j
public class AirQualityScoreCalculationScheduler {

    private final AirQualityScoreService airQualityScoreService;
    private final RoomRepository roomRepository;

    // 시간별 방의 평균 점수만 스케줄러로 계산
    // 배치 처리 : 시간차를 두고 순차적 실행
//    @Scheduled(cron = "0 0 * * * *") // 매시 정각
    public void calculateHourlyRoomAverages() {
        List<Room> roomList = roomRepository.findAll();

        for (Room room : roomList) {
            try {
                // 각 방의 시간별 평균 점수 계산
                airQualityScoreService.calculateHourlyRoomScore(room);
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("방 ID {} 시간별 점수 계산 실패: {}", room.getId(), e.getMessage());
            }
        }
    }

}

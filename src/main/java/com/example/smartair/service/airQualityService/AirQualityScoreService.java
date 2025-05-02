package com.example.smartair.service.airQualityService;

import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airScore.airQualityScore.DeviceAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.PlaceAirQualityScore;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityScoreRepository.DeviceAirQualityScoreRepository;
import com.example.smartair.repository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.airQualityScoreRepository.PlaceAirQualityScoreRepository;
import com.example.smartair.service.airQualityService.calculator.AirQualityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AirQualityScoreService {

    private final AirQualityCalculator airQualityCalculator;
    private final DeviceAirQualityScoreRepository deviceAirQualityScoreRepository;
    private final RoomAirQualityScoreRepository roomAirQualityScoreRepository;
    private final PlaceAirQualityScoreRepository placeAirQualityScoreRepository;

    /**
     * 주어진 AirQualityData를 기반으로 점수 기록을 생성합니다.
     * @param airQualityData
     */
    @Transactional
    public void calculateAndSaveScores(DeviceAirQualityData airQualityData) {
        if (airQualityData == null) {
           throw new CustomException(ErrorCode.INVALID_INPUT_DATA);
        }
        Device device = airQualityData.getDevice();
        if (device == null) {
            throw new CustomException(ErrorCode.DEVICE_NOT_FOUND);
        }
        Room room = device.getRoom();
        if (room == null) {
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        }

        // 1. 개별 DeviceAirQualityScore 계산 및 저장
        DeviceAirQualityScore calculatedDeviceScore = airQualityCalculator.calculateScore(airQualityData);
        calculatedDeviceScore.setDeviceAirQualityData(airQualityData);
        deviceAirQualityScoreRepository.save(calculatedDeviceScore);
        log.info("DeviceAirQualityScore 저장 완료: ID {}", calculatedDeviceScore.getId());

        // 2. RoomAirQualityScore 생성
        createRoomAirQualityScore(room, calculatedDeviceScore);

        // 3. PlaceAirQualityScore 생성
        Place place = room.getPlace();
        if (place != null) {
            createPlaceAirQualityScore(place);
        }
        else{
            log.warn("Room ID {}에 Place 정보가 없어 PlaceAirQualityScore 생성을 건너뛰었습니다.", room.getId());
        }
    }

    /**
     * 특정 방의 새로운 공기질 점수 기록을 생성합니다.
     * @param room 대상 방
     * @param calculatedDeviceScore 계산된 개별 점수 객체 (원본 값 제공 용도)
     */
    private void createRoomAirQualityScore(Room room, DeviceAirQualityScore calculatedDeviceScore) {
        RoomAirQualityScore newRoomScore = new RoomAirQualityScore();
        newRoomScore.setRoom(room);
        newRoomScore.setOverallScore(calculatedDeviceScore.getOverallScore());
        newRoomScore.setPm10Score(calculatedDeviceScore.getPm10Score());
        newRoomScore.setPm25Score(calculatedDeviceScore.getPm25Score());
        newRoomScore.setEco2Score(calculatedDeviceScore.getEco2Score());
        newRoomScore.setTvocScore(calculatedDeviceScore.getTvocScore());

        roomAirQualityScoreRepository.save(newRoomScore);
        log.info("RoomAirQualityScore 생성 완료: Room ID {}, Score ID {}", room.getId(), newRoomScore.getId());
    }

    /**
     * 특정 공간의 새로운 전체 공기질 점수 기록을 생성합니다. (해당 공간 모든 방의 점수 기록 평균)
     * @param place 대상 공간
     */
    private void createPlaceAirQualityScore(Place place) {
        List<RoomAirQualityScore> roomScores = roomAirQualityScoreRepository.findByRoom_Place(place);

        if (roomScores.isEmpty()) {
            log.warn("Place ID {}에 대한 Room 점수 데이터가 없어 PlaceAirQualityScore를 생성하지 않습니다.", place.getId());
            return;
        }

        double avgOverall = roomScores.stream().mapToDouble(RoomAirQualityScore::getOverallScore).average().orElse(0.0);
        double avgPm10 = roomScores.stream().mapToDouble(RoomAirQualityScore::getPm10Score).average().orElse(0.0);
        double avgPm25 = roomScores.stream().mapToDouble(RoomAirQualityScore::getPm25Score).average().orElse(0.0);
        double avgEco2 = roomScores.stream().mapToDouble(RoomAirQualityScore::getEco2Score).average().orElse(0.0);
        double avgTvoc = roomScores.stream().mapToDouble(RoomAirQualityScore::getTvocScore).average().orElse(0.0);

        PlaceAirQualityScore newTotalScore = new PlaceAirQualityScore();
        newTotalScore.setPlace(place);
        newTotalScore.setOverallScore(avgOverall);
        newTotalScore.setPm10Score(avgPm10);
        newTotalScore.setPm25Score(avgPm25);
        newTotalScore.setEco2Score(avgEco2);
        newTotalScore.setTvocScore(avgTvoc);

        placeAirQualityScoreRepository.save(newTotalScore);
        log.info("PlaceAirQualityScore 생성 완료: Place ID {}, Score ID {}", place.getId(), newTotalScore.getId());
    }
}

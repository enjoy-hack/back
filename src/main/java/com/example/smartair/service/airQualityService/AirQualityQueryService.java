package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityScoreDto.AverageScoreDto;
import com.example.smartair.dto.airQualityScoreDto.SensorAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.RoomAirQualityScoreDto;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AirQualityQueryService { //공기질 점수 조회

    private final RoomAirQualityScoreRepository roomAirQualityScoreRepository;
    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;
    private final SensorAirQualityScoreRepository sensorAirQualityScoreRepository;
    private final AirQualityScoreService airQualityScoreService;

    /**
     * 특정 기간의 센서 평균 점수 조회
     * @param serialNumber
     * @param startTime
     * @param endTime
     * @return 평균값 1개
     */
    public AverageScoreDto getSensorAverageScore(String serialNumber,
                                                 LocalDateTime startTime, LocalDateTime endTime) {

        LocalDateTime effectiveStartTime = getDefaultStartTime(startTime);
        LocalDateTime effectiveEndTime = getDefaultEndTime(endTime);

        List<SensorAirQualityScore> scores = sensorAirQualityScoreRepository
                .findScoresBySensorSerialNumberAndTimeRange(
                        serialNumber, effectiveStartTime, effectiveEndTime);

        return airQualityScoreService.calculateAverageDeviceScore(scores);
    }

    /**
     * 특정 기간의 방 평균 점수 조회
     * @param roomId
     * @param startTime
     * @param endTime
     * @return 평균값 1개
     */
    public AverageScoreDto getRoomAverageScore(Long roomId,
                                               LocalDateTime startTime, LocalDateTime endTime) {

        LocalDateTime effectiveStartTime = getDefaultStartTime(startTime);
        LocalDateTime effectiveEndTime = getDefaultEndTime(endTime);

        List<RoomAirQualityScore> scores = roomAirQualityScoreRepository
                .findByRoom_IdAndCreatedAtBetween(
                        roomId, effectiveStartTime, effectiveEndTime);

        return airQualityScoreService.calculateAverageRoomScore(scores);
    }


    /**
     * 특정 센서의 대기질 점수 기록을 시간 범위와 페이징 정보로 조회합니다.
     * @param serialNumber
     * @param startTime 조회 시작 시간
     * @param endTime 조회 종료 시간
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 DeviceAirQualityScoreDto (시계열데이터)
     */
    public Page<SensorAirQualityScoreDto> getSensorAirQualityScores(String serialNumber, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {

        // 기본값: 최근 24시간
        LocalDateTime effectiveStartTime = getDefaultStartTime(startTime);
        LocalDateTime effectiveEndTime = getDefaultEndTime(endTime);


        if (sensorRepository.findBySerialNumber(serialNumber).isEmpty()) {
            throw new CustomException(ErrorCode.SENSOR_NOT_FOUND, "Sensor serialNumber: " + serialNumber);
        }

        return sensorAirQualityScoreRepository
                .findScoresBySensorAndTimeRange(
                        serialNumber,
                        effectiveStartTime,
                        effectiveEndTime,
                        pageable)
                .map(SensorAirQualityScoreDto::fromEntity);
    }

    /**
     * 특정 방의 대기질 점수 기록을 시간 범위와 페이징 정보로 조회합니다.
     * @param roomId 대상 방 ID
     * @param startTime 조회 시작 시간 
     * @param endTime 조회 종료 시간 
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 RoomAirQualityScoreDto (시계열데이터)
     */
    public Page<RoomAirQualityScoreDto> getRoomAirQualityScores(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (!roomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND, "Room ID: " + roomId);
        }

        // 기본값: 최근 24시간
        LocalDateTime effectiveStartTime = getDefaultStartTime(startTime);
        LocalDateTime effectiveEndTime = getDefaultEndTime(endTime);

        return roomAirQualityScoreRepository
                .findScoresByRoomIdAndTimeRange(
                        roomId,
                        effectiveStartTime,
                        effectiveEndTime,
                        pageable)
                .map(RoomAirQualityScoreDto::fromEntity);
    }

//    /**
//     * 특정 공간의 공기질 점수 기록을 시간 범위와 페이징 정보로 조회합니다.
//     * @param placeId 대상 공간 ID
//     * @param startTime 조회 시작 시간
//     * @param endTime 조회 종료 시간
//     * @param pageable 페이징 및 정렬 정보
//     * @return 페이징된 PlaceAirQualityScoreDto
//     */
//    public Page<PlaceAirQualityScoreDto> getPlaceAirQualityScores(Long placeId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
//        if (!placeRepository.existsById(placeId)) {
//            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
//        }
//        Page<PlaceAirQualityScore> scoreEntityPage = placeAirQualityScoreRepository.findScoresByPlaceIdAndTimeRange(placeId, startTime, endTime, pageable);
//
//        return scoreEntityPage.map(PlaceAirQualityScoreDto::fromEntity);
//    }

    public SensorAirQualityScoreDto getLatestSensorAirQualityScore(String serialNumber) {
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber).orElseThrow(()-> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "Sensor ID: " + serialNumber));

        SensorAirQualityScore latestSensorScore = sensorAirQualityScoreRepository.findFirstBySensorAirQualityData_SensorOrderByCreatedAtDesc(sensor).orElseThrow(
                ()-> new CustomException(ErrorCode.SENSOR_AIR_DATA_NOT_FOUND, "Sensor serialNumber: " + serialNumber)
        );

        return SensorAirQualityScoreDto.fromEntity(latestSensorScore);
    }

    /**
     * 대상 방의 가장 최신 대기질 데이터를 조회합니다.
     * @param roomId 대상 방 ID
     * @return RoomAirQualityScoreDto
     */
    public RoomAirQualityScoreDto getLatestRoomAirQualityScore(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()-> new CustomException(ErrorCode.ROOM_NOT_FOUND, "Room ID: " + roomId));

        RoomAirQualityScore latestRoomScore = roomAirQualityScoreRepository.findFirstByRoomOrderByCreatedAtDesc(room).orElseThrow(
                () -> new CustomException(ErrorCode.ROOM_SCORE_NOT_FOUND, "Room air quality score not found for ID: " + roomId)
        );

        return RoomAirQualityScoreDto.fromEntity(latestRoomScore);
    }

//    /**
//     * 대상 공간의 가장 최신의 공기질 데이터를 조회합니다.
//     * @param placeId 대상 공간 ID
//     * @return PlaceAirQualityScoreDto
//     */
//    public PlaceAirQualityScoreDto getLatestPlaceAirQualityScore(Long placeId) {
//        Place place = placeRepository.findById(placeId).orElseThrow(()-> new CustomException(ErrorCode.PLACE_NOT_FOUND));
//
//        PlaceAirQualityScore latestPlaceScore = placeAirQualityScoreRepository.findFirstByPlaceOrderByCreatedAtDesc(place).orElseThrow(
//                () -> new CustomException(ErrorCode.PLACE_SCORE_NOT_FOUND)
//        );
//
//        return PlaceAirQualityScoreDto.fromEntity(latestPlaceScore);
//    }

    public LocalDateTime getDefaultStartTime(LocalDateTime startTime) {
        return startTime != null ? startTime : LocalDateTime.now().minusHours(24);
    }

    public LocalDateTime getDefaultEndTime(LocalDateTime endTime) {
        return endTime != null ? endTime : LocalDateTime.now();
    }

}


package com.example.smartair.service.airQualityService;

import com.example.smartair.dto.airQualityScoreDto.DeviceAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.PlaceAirQualityScoreDto;
import com.example.smartair.dto.airQualityScoreDto.RoomAirQualityScoreDto;
import com.example.smartair.entity.airScore.airQualityScore.DeviceAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.PlaceAirQualityScore;
import com.example.smartair.entity.airScore.airQualityScore.RoomAirQualityScore;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.place.Place;
import com.example.smartair.entity.room.Room;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.DeviceAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.PlaceAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.RoomAirQualityScoreRepository;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.placeRepository.PlaceRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AirQualityQueryService {

    private final DeviceAirQualityScoreRepository deviceAirQualityScoreRepository;
    private final RoomAirQualityScoreRepository roomAirQualityScoreRepository;
    private final PlaceAirQualityScoreRepository placeAirQualityScoreRepository;
    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final PlaceRepository placeRepository;

    /**
     * 특정 디바이스의 공기질 점수 기록을 시간 범위와 페이징 정보로 조회합니다.
     * @param deviceId 대상 디바이스 ID
     * @param startTime 조회 시작 시간
     * @param endTime 조회 종료 시간
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 DeviceAirQualityScoreDto
     */
    public Page<DeviceAirQualityScoreDto> getDeviceAirQualityScores(Long deviceId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (!deviceRepository.existsById(deviceId)) {
            throw new CustomException(ErrorCode.DEVICE_NOT_FOUND);
        }

        Page<DeviceAirQualityScore> scoreEntityPage = deviceAirQualityScoreRepository.findScoresByDeviceAndTimeRange(
                deviceId, startTime, endTime, pageable);

        return scoreEntityPage.map(DeviceAirQualityScoreDto::fromEntity);
    }

    /**
     * 특정 방의 공기질 점수 기록을 시간 범위와 페이징 정보로 조회합니다.
     * @param roomId 대상 방 ID
     * @param startTime 조회 시작 시간 
     * @param endTime 조회 종료 시간 
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 RoomAirQualityScoreDto
     */
    public Page<RoomAirQualityScoreDto> getRoomAirQualityScores(Long roomId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (!roomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.ROOM_NOT_FOUND);
        }
        Page<RoomAirQualityScore> scoreEntityPage = roomAirQualityScoreRepository.findScoresByRoomIdAndTimeRange(roomId, startTime, endTime, pageable);
        
        return scoreEntityPage.map(RoomAirQualityScoreDto::fromEntity);
    }

    /**
     * 특정 공간의 공기질 점수 기록을 시간 범위와 페이징 정보로 조회합니다.
     * @param placeId 대상 공간 ID
     * @param startTime 조회 시작 시간 
     * @param endTime 조회 종료 시간 
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 PlaceAirQualityScoreDto
     */
    public Page<PlaceAirQualityScoreDto> getPlaceAirQualityScores(Long placeId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (!placeRepository.existsById(placeId)) {
            throw new CustomException(ErrorCode.PLACE_NOT_FOUND);
        }
        Page<PlaceAirQualityScore> scoreEntityPage = placeAirQualityScoreRepository.findScoresByPlaceIdAndTimeRange(placeId, startTime, endTime, pageable);

        return scoreEntityPage.map(PlaceAirQualityScoreDto::fromEntity);
    }

    public DeviceAirQualityScoreDto getLatestDeviceAirQualityScore(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(()-> new CustomException(ErrorCode.DEVICE_NOT_FOUND));

        DeviceAirQualityScore latestDeviceScore = deviceAirQualityScoreRepository.findFirstByDeviceAirQualityData_DeviceOrderByCreatedAtDesc(device).orElseThrow(
                ()-> new CustomException(ErrorCode.DEVICE_NOT_FOUND)
        );

        return DeviceAirQualityScoreDto.fromEntity(latestDeviceScore);
    }

    /**
     * 대상 방의 가장 최신의 공기질 데이터를 조회합니다.
     * @param roomId 대상 방 ID
     * @return RoomAirQualityScoreDto
     */
    public RoomAirQualityScoreDto getLatestRoomAirQualityScore(Long roomId) {
        Room room = roomRepository.findById(roomId).orElseThrow(()-> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        RoomAirQualityScore latestRoomScore = roomAirQualityScoreRepository.findFirstByRoomOrderByCreatedAtDesc(room).orElseThrow(
                () -> new CustomException(ErrorCode.ROOM_SCORE_NOT_FOUND)
        );

        return RoomAirQualityScoreDto.fromEntity(latestRoomScore);
    }

    /**
     * 대상 공간의 가장 최신의 공기질 데이터를 조회합니다.
     * @param placeId 대상 공간 ID
     * @return PlaceAirQualityScoreDto
     */
    public PlaceAirQualityScoreDto getLatestPlaceAirQualityScore(Long placeId) {
        Place place = placeRepository.findById(placeId).orElseThrow(()-> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        PlaceAirQualityScore latestPlaceScore = placeAirQualityScoreRepository.findFirstByPlaceOrderByCreatedAtDesc(place).orElseThrow(
                () -> new CustomException(ErrorCode.PLACE_SCORE_NOT_FOUND)
        );

        return PlaceAirQualityScoreDto.fromEntity(latestPlaceScore);
    }


}


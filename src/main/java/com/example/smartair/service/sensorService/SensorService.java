package com.example.smartair.service.sensorService;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.dto.sensorDto.SensorResponseDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.report.WeeklySensorAirQualityReport;
import com.example.smartair.entity.airData.snapshot.HourlySensorAirQualitySnapshot;
import com.example.smartair.entity.airScore.airQualityScore.SensorAirQualityScore;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.SensorAirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityScoreRepository.SensorAirQualityScoreRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.WeeklySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualitySnapshotRepository.HourlySensorAirQualitySnapshotRepository;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.service.airQualityService.AirQualityScoreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class SensorService {

    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;
    private final RoomSensorRepository roomSensorRepository;
    private final FineParticlesDataRepository fineParticlesDataRepository;
    private final FineParticlesDataPt2Repository fineParticlesDataPt2Repository;
    private final DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;
    private final WeeklySensorAirQualityReportRepository weeklySensorAirQualityReportRepository;
    private final PredictedAirQualityRepository predictedAirQualityRepository;
    private final SensorAirQualityDataRepository sensorAirQualityDataRepository;
    private final SensorAirQualityScoreRepository sensorAirQualityScoreRepository;
    private final AirQualityScoreService airQualityScoreService;
    private final HourlySensorAirQualitySnapshotRepository hourlySensorAirQualitySnapshotRepository;

    public Sensor setSensor(User user, SensorRequestDto.setSensorDto sensorRequestDto) throws Exception {
        Sensor sensor = Sensor.builder()
                .name(sensorRequestDto.name())
                .serialNumber(sensorRequestDto.serialNumber())
                .user(user)
                .runningStatus(false)
                .isRegistered(false) //처음 등록시 방에 등록되지 않은 상태
                .roomRegisterDate(null) // 방에 등록되지 않으니 null
                .build();

        sensorRepository.save(sensor);
        return sensor;
    }

    public RoomSensor addSensorToRoom(User user, SensorRequestDto.addSensorToRoomDto sensorDto) throws Exception {
        Room room = roomRepository.findRoomById(sensorDto.roomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        Sensor sensor = sensorRepository.findBySerialNumber(sensorDto.serialNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND));

//        // 센서 소유자 검증
//        if (!sensor.getUser().getId().equals(user.getId())) {
//            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR);
//        }

//        // 등록하려는 유저가 방에 등록된 사람인지 확인
//        if (!roomRepository.existsByIdAndParticipants_User(room.getId(), user)) {
//            throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM);
//        }

        // 이미 방에 등록되어 있는 센서인지 확인
        if (roomSensorRepository.existsBySensor_SerialNumberAndRoom_Id(sensorDto.serialNumber(), room.getId())) {
            throw new CustomException(ErrorCode.SENSOR_ALREADY_EXIST_IN_ROOM);
        }
        sensor.setRegistered(true);
        sensor.setRoomRegisterDate(LocalDateTime.now()); // 방에 등록된 시점으로 설정
        sensorRepository.save(sensor);

        RoomSensor roomSensor = RoomSensor.builder()
                .sensor(sensor)
                .room(room)
                .build();

        roomSensorRepository.save(roomSensor);
        return roomSensor;
    }

    public void deleteSensor(User user, String serialNumber) {
            Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                    .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND));

//        // 센서 소유자 검증
//        if (!sensor.getUser().getId().equals(user.getId())) {
//            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR, "센서의 소유자가 아닙니다.");
//        }
            List<Room> affectedRooms = deleteRelatedEntities(sensor);

            sensorRepository.delete(sensor);

            updateAffectedRoomScores(serialNumber, affectedRooms);
    }

    public List<Room> deleteRelatedEntities(Sensor sensor){
        List<RoomSensor> roomSensors = roomSensorRepository.findAllBySensor_Id(sensor.getId());
        List<Room> affectedRooms = roomSensors.stream()
                .map(RoomSensor::getRoom)
                .distinct()
                .toList();
        roomSensorRepository.deleteAll(roomSensors);

        // 실시간 데이터 삭제
        deleteRealTimeData(sensor);

        // 공기질 데이터 및 점수 삭제
        deleteAirQualityData(sensor);

        // 리포트 데이터 삭제
        deleteReportData(sensor);

        // 예측 데이터 삭제
        deletePredictionData(sensor);

        return affectedRooms;
    }

    public boolean getSensorStatus(String serialNumber) throws Exception {
        Optional<Sensor> optionalSensor = sensorRepository.findBySerialNumber(serialNumber);

        if (optionalSensor.isEmpty()) throw new CustomException(ErrorCode.INVALID_REQUEST, "해당 센서가 존재하지 않습니다.");

        return optionalSensor.get().isRunningStatus();
    }

    public void unregisterSensorFromRoom(User user, String serialNumber, Long roomId) throws Exception {
        Room room = roomRepository.findRoomById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

//        // 등록된 참여자인지 확인
//        if (!roomRepository.existsByIdAndParticipants_User(room.getId(), user)) {
//            throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM, "해당 방에 등록된 사용자가 아닙니다.");
//        }

        RoomSensor roomSensor = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(serialNumber, roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_SENSOR_MAPPING_NOT_FOUND));

//        // 센서 소유자 검증
//        if (!roomSensor.getSensor().getUser().getId().equals(user.getId())) {
//            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR, "해당 센서에 대한 권한이 없습니다.");
//        }

        // 센서의 등록 상태 변경
        Sensor sensor = roomSensor.getSensor();
        sensor.setRegistered(false);
        sensor.setRoomRegisterDate(null); // 방에 등록되지 않은 상태로 변경
        sensorRepository.save(sensor);

        // RoomSensor 매핑 삭제
        roomSensorRepository.delete(roomSensor);
    }

    public SensorResponseDto getSensorById(Long sensorId) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND));

        return SensorResponseDto.from(sensor);
    }

    public List<SensorResponseDto> getUserSensors(User user) {
        List<Sensor> sensors = sensorRepository.findByUser(user);

        return sensors.stream()
                .map(SensorResponseDto::from)
                .toList();

    }

    public SensorResponseDto getSensorBySerialNumber(String serialNumber) {
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND));

        return SensorResponseDto.from(sensor);
    }

    public void deleteRealTimeData(Sensor sensor){
        Optional<FineParticlesData> optionalFine = fineParticlesDataRepository.findBySensor_Id(sensor.getId());
        optionalFine.ifPresent(fineParticlesDataRepository::delete);

        Optional<FineParticlesDataPt2> optionalFine2 = fineParticlesDataPt2Repository.findBySensor_Id(sensor.getId());
        optionalFine2.ifPresent(fineParticlesDataPt2Repository::delete);
    }

    public void deleteAirQualityData(Sensor sensor){
        List<SensorAirQualityData> qualityDataList = sensorAirQualityDataRepository.findAllBySensor_Id(sensor.getId());

        for (SensorAirQualityData data : qualityDataList) {
            // 각 데이터에 연결된 점수 삭제
            List<SensorAirQualityScore> scores = sensorAirQualityScoreRepository.findAllBySensorAirQualityDataId(data.getId());
            sensorAirQualityScoreRepository.deleteAll(scores);
        }

        // 모든 공기질 데이터 삭제
        sensorAirQualityDataRepository.deleteAll(qualityDataList);
    }

    public void deleteReportData(Sensor sensor){
        //hourlysnapshot 데이터 삭제
        List<HourlySensorAirQualitySnapshot> hourlySnapshots = hourlySensorAirQualitySnapshotRepository.findAllBySensor_Id(sensor.getId());
        hourlySensorAirQualitySnapshotRepository.deleteAll(hourlySnapshots);
        //하루 평균 데이터 삭제
        List<DailySensorAirQualityReport> dailyReports = dailySensorAirQualityReportRepository.findAllBySensorId(sensor.getId());
        dailySensorAirQualityReportRepository.deleteAll(dailyReports);
        //일주일 평균 데이터 삭제
        List<WeeklySensorAirQualityReport> weeklyReports = weeklySensorAirQualityReportRepository.findAllBySensorId(sensor.getId());
        weeklySensorAirQualityReportRepository.deleteAll(weeklyReports);
    }

    public void deletePredictionData(Sensor sensor){
        //예측된 공기질 데이터 삭제
        List<PredictedAirQualityData> predictedAirQualityDataList = predictedAirQualityRepository.findAllBySensorSerialNumber(sensor.getSerialNumber());
        predictedAirQualityRepository.deleteAll(predictedAirQualityDataList);
    }

    public void updateAffectedRoomScores(String serialNumber, List<Room> affectedRooms) {
        for (Room room : affectedRooms) {
            try{
                airQualityScoreService.updateRoomAverageScore(room);
                log.info("센서 SerialNumber{} 삭제로 인한 방 {}의 평균 점수 업데이트 완료", serialNumber, room.getId());
            }
            catch (Exception e){
                log.error("센서 SerialNumber{} 삭제로 인한 방 {}의 평균 점수 업데이트 중 오류 발생: {}", serialNumber, room.getId(), e.getMessage());
            }
        }
    }

}

package com.example.smartair.service.sensorService;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.dto.sensorDto.SensorResponseDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.airData.predictedAirQualityData.PredictedAirQualityData;
import com.example.smartair.entity.airData.report.DailySensorAirQualityReport;
import com.example.smartair.entity.airData.report.WeeklySensorAirQualityReport;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.DailySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.airQualityReportRepository.WeeklySensorAirQualityReportRepository;
import com.example.smartair.repository.airQualityRepository.predictedAirQualityRepository.PredictedAirQualityRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class SensorService {

    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;
    private final RoomSensorRepository roomSensorRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final FineParticlesDataRepository fineParticlesDataRepository;
    private final FineParticlesDataPt2Repository fineParticlesDataPt2Repository;
    private final DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;
    private final WeeklySensorAirQualityReportRepository weeklySensorAirQualityReportRepository;
    private final PredictedAirQualityRepository predictedAirQualityRepository;
    private final UserRepository userRepository;

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
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "roomId에 맞는 방이 없습니다."));

        Sensor sensor = sensorRepository.findBySerialNumber(sensorDto.serialNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.SENSOR_NOT_FOUND, "serialNumber에 맞는 센서가 없습니다."));

        // 센서 소유자 검증
        if (!sensor.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR, "해당 센서에 대한 권한이 없습니다.");
        }

        // 등록하려는 유저가 방에 등록된 사람인지 확인
        if (!roomRepository.existsByIdAndParticipants_User(room.getId(), user)) {
            throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM, "해당 방에 등록된 사용자가 아닙니다.");
        }

        // 이미 방에 등록되어 있는 센서인지 확인
        if (roomSensorRepository.existsBySensor_SerialNumberAndRoom_Id(sensorDto.serialNumber(), room.getId())) {
            throw new CustomException(ErrorCode.SENSOR_ALREADY_EXIST_IN_ROOM, "이미 방에 등록된 센서입니다.");
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

    public void deleteSensor(User user, SensorRequestDto.deleteSensorDto sensorDto) throws Exception {
        Room room = roomRepository.findRoomById(sensorDto.roomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "roomId에 맞는 방이 없습니다."));

        // 등록된 참여자인지 확인
        if (!roomRepository.existsByIdAndParticipants_User(room.getId(), user)) {
            throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM, "해당 방에 등록된 사용자가 아닙니다.");
        }

        RoomSensor roomSensor = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(sensorDto.serialNumber(), sensorDto.roomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_SENSOR_MAPPING_NOT_FOUND, "roomId에 맞는 방에 등록된 센서가 없습니다."));

        Sensor sensor = roomSensor.getSensor();

        // 센서 소유자 검증
        if (!sensor.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR, "해당 센서에 대한 권한이 없습니다.");
        }

        //실시간 데이터 삭제
        Optional<FineParticlesData> optionalFine = fineParticlesDataRepository.findBySensor_Id(sensor.getId());
        optionalFine.ifPresent(fineParticlesDataRepository::delete);

        Optional<FineParticlesDataPt2> optionalFine2 = fineParticlesDataPt2Repository.findBySensor_Id(sensor.getId());
        optionalFine2.ifPresent(fineParticlesDataPt2Repository::delete);

        Optional<SensorAirQualityData> qualityDataOptional = airQualityDataRepository.findBySensor_Id(sensor.getId());
        qualityDataOptional.ifPresent(airQualityDataRepository::delete);

        //하루 평균 데이터 삭제
        List<DailySensorAirQualityReport> dailyReports = dailySensorAirQualityReportRepository.findAllBySensorId(sensor.getId());
        dailySensorAirQualityReportRepository.deleteAll(dailyReports);
        //일주일 평균 데이터 삭제
        List<WeeklySensorAirQualityReport> weeklyReports = weeklySensorAirQualityReportRepository.findAllBySensorId(sensor.getId());
        weeklySensorAirQualityReportRepository.deleteAll(weeklyReports);

        //예측된 공기질 데이터 삭제
        List<PredictedAirQualityData> predictedAirQualityDataList = predictedAirQualityRepository.findBySensorSerialNumber(sensor.getSerialNumber());
        predictedAirQualityRepository.deleteAll(predictedAirQualityDataList);

        sensorRepository.delete(sensor);
        roomSensorRepository.delete(roomSensor);
    }

    public List<SensorResponseDto> getSensors(Long roomId, User user) {
        // 방이 존재하는지 확인
        roomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

       // 사용자가 방에 등록된 참여자인지 확인
        if (!roomRepository.existsByIdAndParticipants_User(roomId, user)) {
            throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM, "해당 방에 등록된 사용자가 아닙니다.");
        }

        // 해당 방에 등록된 센서만 조회
        List<Sensor> sensors = roomSensorRepository.findByRoomId(roomId)
                .stream()
                .map(RoomSensor::getSensor)
                .toList();

        // 센서 정보를 DTO로 변환하여 반환
        return sensors.stream()
                .map(SensorResponseDto::from)
                .toList();
    }

    public boolean getSensorStatus(String serialNumber) throws Exception {
        Optional<Sensor> optionalSensor = sensorRepository.findBySerialNumber(serialNumber);

        if (optionalSensor.isEmpty()) throw new CustomException(ErrorCode.INVALID_REQUEST, "해당 센서가 존재하지 않습니다.");

        return optionalSensor.get().isRunningStatus();
    }

    public void unregisterSensorFromRoom(User user, SensorRequestDto.unregisterSensorFromRoomDto request) throws Exception {
        Room room = roomRepository.findRoomById(request.roomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND, "roomId에 맞는 방이 없습니다."));

        // 등록된 참여자인지 확인
        if (!roomRepository.existsByIdAndParticipants_User(room.getId(), user)) {
            throw new CustomException(ErrorCode.PARTICIPANT_NOT_FOUND_IN_ROOM, "해당 방에 등록된 사용자가 아닙니다.");
        }

        RoomSensor roomSensor = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(request.serialNumber(), request.roomId())
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_SENSOR_MAPPING_NOT_FOUND, "roomId에 맞는 방에 등록된 센서가 없습니다."));

        // 센서 소유자 검증
        if (!roomSensor.getSensor().getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.NO_AUTHORITY_TO_ACCESS_SENSOR, "해당 센서에 대한 권한이 없습니다.");
        }

        // 센서의 등록 상태 변경
        Sensor sensor = roomSensor.getSensor();
        sensor.setRegistered(false);
        sensor.setRoomRegisterDate(null); // 방에 등록되지 않은 상태로 변경
        sensorRepository.save(sensor);

        //실시간 데이터 삭제
        Optional<FineParticlesData> optionalFine = fineParticlesDataRepository.findBySensor_Id(sensor.getId());
        optionalFine.ifPresent(fineParticlesDataRepository::delete);

        Optional<FineParticlesDataPt2> optionalFine2 = fineParticlesDataPt2Repository.findBySensor_Id(sensor.getId());
        optionalFine2.ifPresent(fineParticlesDataPt2Repository::delete);

        Optional<SensorAirQualityData> qualityDataOptional = airQualityDataRepository.findBySensor_Id(sensor.getId());
        qualityDataOptional.ifPresent(airQualityDataRepository::delete);

        //하루 평균 데이터 삭제
        List<DailySensorAirQualityReport> dailyReports = dailySensorAirQualityReportRepository.findAllBySensorId(sensor.getId());
        dailySensorAirQualityReportRepository.deleteAll(dailyReports);
        //일주일 평균 데이터 삭제
        List<WeeklySensorAirQualityReport> weeklyReports = weeklySensorAirQualityReportRepository.findAllBySensorId(sensor.getId());
        weeklySensorAirQualityReportRepository.deleteAll(weeklyReports);

        // 예측된 공기질 데이터 삭제
        List<PredictedAirQualityData> predictedAirQualityDataList = predictedAirQualityRepository.findBySensorSerialNumber(sensor.getSerialNumber());
        predictedAirQualityRepository.deleteAll(predictedAirQualityDataList);

        // RoomSensor 매핑 삭제
        roomSensorRepository.delete(roomSensor);
    }
}

package com.example.smartair.service.sensorService;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.airData.airQualityData.SensorAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
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
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomSensorRepository.RoomSensorRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;
    private final RoomSensorRepository roomSensorRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final FineParticlesDataRepository fineParticlesDataRepository;
    private final FineParticlesDataPt2Repository fineParticlesDataPt2Repository;
    private final DailySensorAirQualityReportRepository dailySensorAirQualityReportRepository;
    private final WeeklySensorAirQualityReportRepository weeklySensorAirQualityReportRepository;

    public Sensor setSensor(User user, SensorRequestDto.setSensorDto sensorRequestDto) throws Exception {

        Optional<Room> optionalRoom = roomRepository.findRoomById(sensorRequestDto.roomId());
        if(optionalRoom.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_NOT_FOUND));
        //방이 존재하지 않은 경우

        Room room = optionalRoom.get();

        if(!room.getOwner().getId().equals(user.getId())) throw new Exception(new CustomException(ErrorCode.NO_AUTHORITY));

        Optional<RoomSensor>  optionalRoomSensor = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(
                sensorRequestDto.serialNumber(),room.getId());
        if(optionalRoomSensor.isPresent()) throw new Exception(new CustomException(ErrorCode.SENSOR_ALREADY_EXIST_IN_ROOM));
        //이미 디바이스가 방에 연결되어 있는 경우

        optionalRoomSensor = roomSensorRepository.findBySensor_SerialNumber(sensorRequestDto.serialNumber());
        if(optionalRoomSensor.isPresent()) throw new Exception(new CustomException(ErrorCode.SENSOR_ALREADY_EXIST_IN_ANOTHER_ROOM));
        // 다른 방에 연결되어있는 경우

        Sensor sensor = Sensor.builder()
                .name(sensorRequestDto.name())
                .serialNumber(sensorRequestDto.serialNumber())
                .user(user)
                .runningStatus(false)
                .build();

        sensorRepository.save(sensor);

        RoomSensor roomSensor = RoomSensor.builder()
                .sensor(sensor)
                .room(optionalRoom.get())
                .build();

        roomSensorRepository.save(roomSensor);
        return sensor;
    }

    public void deleteSensor(User user, SensorRequestDto.deleteSensorDto sensorDto) throws Exception {
        Room room = roomRepository.findRoomById(sensorDto.roomId())
                .orElseThrow(() -> new Exception(new CustomException(ErrorCode.ROOM_NOT_FOUND)));

        if(!room.getOwner().equals(user)) throw new Exception(new CustomException(ErrorCode.NO_AUTHORITY));

        Optional<RoomSensor> optionalRoomSensor = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(sensorDto.serialNumber(), sensorDto.roomId());
        if(optionalRoomSensor.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_SENSOR_MAPPING_NOT_FOUND));

        RoomSensor roomSensor = optionalRoomSensor.get();
        Sensor sensor = roomSensor.getSensor();

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

        sensorRepository.delete(sensor);
        roomSensorRepository.delete(roomSensor);
    }

    public List<Sensor> getSensors(Long roomId){

        List<RoomSensor> roomSensors = roomSensorRepository.findByRoomId(roomId);

        // RoomDevice → Device 추출
        return roomSensors.stream()
                .map(RoomSensor::getSensor)
                .collect(Collectors.toList());
    }

    public boolean getSensorStatus(Long serialNumber) throws Exception {
        Optional<Sensor> optionalSensor = sensorRepository.findBySerialNumber(serialNumber);

        if(optionalSensor.isEmpty()) throw new Exception(new CustomException(ErrorCode.INVALID_REQUEST));

        return optionalSensor.get().isRunningStatus();
    }

}
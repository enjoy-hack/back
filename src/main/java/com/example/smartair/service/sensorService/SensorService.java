package com.example.smartair.service.sensorService;

import com.example.smartair.dto.sensorDto.SensorRequestDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomSensor.RoomSensor;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityRepository.airQualityDataRepository.FineParticlesDataRepository;
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

    public void setDevice(User user, SensorRequestDto.setDeviceDto deviceRequestDto) throws Exception {

        Optional<Room> optionalRoom = roomRepository.findRoomById(deviceRequestDto.roomId());
        if(optionalRoom.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_NOT_FOUND));
        //방이 존재하지 않은 경우

        Room room = optionalRoom.get();

        Optional<RoomSensor>  optionalRoomDevice = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(
                deviceRequestDto.serialNumber(),room.getId());
        if(optionalRoomDevice.isPresent()) throw new Exception(new CustomException(ErrorCode.DEVICE_ALREADY_EXIST_IN_ROOM));
        //이미 디바이스가 방에 연결되어 있는 경우

        optionalRoomDevice = roomSensorRepository.findBySensor_SerialNumber(deviceRequestDto.serialNumber());
        if(optionalRoomDevice.isPresent()) throw new Exception(new CustomException(ErrorCode.DEVICE_ALREADY_EXIST_IN_ANOTHER_ROOM));
        // 다른 방에 연결되어있는 경우

        Sensor sensor = Sensor.builder()
                .name(deviceRequestDto.name())
                .serialNumber(deviceRequestDto.serialNumber())
                .user(user)
                .runningStatus(false)
                .build();

        sensorRepository.save(sensor);

        RoomSensor roomSensor = RoomSensor.builder()
                .sensor(sensor)
                .room(optionalRoom.get())
                .build();

        roomSensorRepository.save(roomSensor);
    }

    public void deleteDevice(User user, SensorRequestDto.deleteDeviceDto deviceDto) throws Exception {
        Optional<RoomSensor> optionalRoomDevice = roomSensorRepository.findBySensor_SerialNumberAndRoom_Id(deviceDto.serialNumber(), deviceDto.roomId());
        if(optionalRoomDevice.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_DEVICE_MAPPING_NOT_FOUND));

        RoomSensor roomSensor = optionalRoomDevice.get();
        Sensor sensor = roomSensor.getSensor();

        //실시간 데이터 삭제
        Optional<FineParticlesData> optionalFine = fineParticlesDataRepository.findBySensor_Id(sensor.getId());
        optionalFine.ifPresent(fineParticlesDataRepository::delete);

        Optional<FineParticlesDataPt2> optionalFine2 = fineParticlesDataPt2Repository.findBySensor_Id(sensor.getId());
        optionalFine2.ifPresent(fineParticlesDataPt2Repository::delete);

        Optional<DeviceAirQualityData> qualityDataOptional = airQualityDataRepository.findBySensor_Id(sensor.getId());
        qualityDataOptional.ifPresent(airQualityDataRepository::delete);

        //추후, 일주일 평균 데이터 삭제
        

        sensorRepository.delete(sensor);
        roomSensorRepository.delete(roomSensor);
    }

    public List<Sensor> getDevices(Long roomId){

        List<RoomSensor> roomSensors = roomSensorRepository.findByRoomId(roomId);

        // RoomDevice → Device 추출
        return roomSensors.stream()
                .map(RoomSensor::getSensor)
                .collect(Collectors.toList());
    }

    public boolean getDeviceStatus(Long serialNumber) throws Exception {
        Optional<Sensor> optionalDevice = sensorRepository.findBySerialNumber(serialNumber);

        if(optionalDevice.isEmpty()) throw new Exception(new CustomException(ErrorCode.INVALID_REQUEST));

        return optionalDevice.get().isRunningStatus();
    }

}
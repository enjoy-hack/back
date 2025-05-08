package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceRequestDto;
import com.example.smartair.entity.airData.airQualityData.DeviceAirQualityData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesData;
import com.example.smartair.entity.airData.fineParticlesData.FineParticlesDataPt2;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.airQualityDataRepository.AirQualityDataRepository;
import com.example.smartair.repository.airQualityDataRepository.FineParticlesDataPt2Repository;
import com.example.smartair.repository.airQualityDataRepository.FineParticlesDataRepository;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.roomDeviceRepository.RoomDeviceRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final RoomDeviceRepository roomDeviceRepository;
    private final AirQualityDataRepository airQualityDataRepository;
    private final FineParticlesDataRepository fineParticlesDataRepository;
    private final FineParticlesDataPt2Repository fineParticlesDataPt2Repository;

    public void setDevice(User user, DeviceRequestDto.setDeviceDto deviceRequestDto) throws Exception {

        Optional<Room> optionalRoom = roomRepository.findRoomById(deviceRequestDto.roomId());
        if(optionalRoom.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_NOT_FOUND));
        //방이 존재하지 않은 경우

        Room room = optionalRoom.get();

        Optional<RoomDevice>  optionalRoomDevice = roomDeviceRepository.findByDevice_SerialNumberAndRoom_Id(
                deviceRequestDto.serialNumber(),room.getId());
        if(optionalRoomDevice.isPresent()) throw new Exception(new CustomException(ErrorCode.DEVICE_ALREADY_EXIST_IN_ROOM));
        //이미 디바이스가 방에 연결되어 있는 경우

        optionalRoomDevice = roomDeviceRepository.findByDevice_SerialNumber(deviceRequestDto.serialNumber());
        if(optionalRoomDevice.isPresent()) throw new Exception(new CustomException(ErrorCode.DEVICE_ALREADY_EXIST_IN_ANOTHER_ROOM));
        // 다른 방에 연결되어있는 경우

        Device device = Device.builder()
                .name(deviceRequestDto.name())
                .serialNumber(deviceRequestDto.serialNumber())
                .user(user)
                .runningStatus(false)
                .build();

        deviceRepository.save(device);

        RoomDevice roomDevice = RoomDevice.builder()
                .device(device)
                .room(optionalRoom.get())
                .build();

        roomDeviceRepository.save(roomDevice);
    }

    public void deleteDevice(User user, DeviceRequestDto.deleteDeviceDto deviceDto) throws Exception {
        Optional<RoomDevice> optionalRoomDevice = roomDeviceRepository.findByDevice_SerialNumberAndRoom_Id(deviceDto.serialNumber(), deviceDto.roomId());
        if(optionalRoomDevice.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_DEVICE_MAPPING_NOT_FOUND));

        RoomDevice roomDevice = optionalRoomDevice.get();
        Device device = roomDevice.getDevice();

        //실시간 데이터 삭제
        Optional<FineParticlesData> optionalFine = fineParticlesDataRepository.findByDevice_Id(device.getId());
        optionalFine.ifPresent(fineParticlesDataRepository::delete);

        Optional<FineParticlesDataPt2> optionalFine2 = fineParticlesDataPt2Repository.findByDevice_Id(device.getId());
        optionalFine2.ifPresent(fineParticlesDataPt2Repository::delete);

        Optional<DeviceAirQualityData> qualityDataOptional = airQualityDataRepository.findByDevice_Id(device.getId());
        qualityDataOptional.ifPresent(airQualityDataRepository::delete);

        //추후, 일주일 평균 데이터 삭제

        deviceRepository.delete(device);
        roomDeviceRepository.delete(roomDevice);
    }

    public List<Device> getDevices(Long roomId){

        List<RoomDevice> roomDevices = roomDeviceRepository.findByRoomId(roomId);

        // RoomDevice → Device 추출
        return roomDevices.stream()
                .map(RoomDevice::getDevice)
                .collect(Collectors.toList());
    }

    public boolean getDeviceStatus(Long serialNumber) throws Exception {
        Optional<Device> optionalDevice = deviceRepository.findBySerialNumber(serialNumber);

        if(optionalDevice.isEmpty()) throw new Exception(new CustomException(ErrorCode.INVALID_REQUEST));

        return optionalDevice.get().isRunningStatus();
    }

}

package com.example.smartair.service.deviceService;

import com.example.smartair.dto.deviceDto.DeviceRequestDto;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.room.Room;
import com.example.smartair.entity.roomDevice.RoomDevice;
import com.example.smartair.entity.user.User;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.roomDeviceRepository.RoomDeviceRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final RoomDeviceRepository roomDeviceRepository;

    public void setDevice(User user, DeviceRequestDto deviceRequestDto) throws Exception {

        Optional<Room> optionalRoom = roomRepository.findRoomById(deviceRequestDto.getRoomId());
        if(optionalRoom.isEmpty()) throw new Exception(new CustomException(ErrorCode.ROOM_NOT_FOUND));
        //방이 존재하지 않은 경우

        Room room = optionalRoom.get();

        Optional<RoomDevice> optionalRoomDevice = roomDeviceRepository.findByDevice_SerialNumberAndRoom_Id(
                deviceRequestDto.getSerialNumber(),room.getId());
        if(optionalRoomDevice.isPresent()) throw new Exception(new CustomException(ErrorCode.DEVICE_ALREADY_EXIST_IN_ROOM));
        //이미 디바이스가 방에 연결되어 있는 경우

        Device device = Device.builder()
                .name(deviceRequestDto.getName())
                .serialNumber(deviceRequestDto.getSerialNumber())
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

//    public void deleteDevice(User user, Long deviceSerialNumber){
//        deviceRepository.
//    }

}

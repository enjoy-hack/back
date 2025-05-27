package com.example.smartair.service.adminService;

import com.example.smartair.dto.deviceDto.DeviceDetailDto;
import com.example.smartair.dto.sensorDto.SensorDetailDto;
import com.example.smartair.dto.sensorDto.SensorResponseDto;
import com.example.smartair.entity.device.Device;
import com.example.smartair.entity.sensor.Sensor;
import com.example.smartair.exception.CustomException;
import com.example.smartair.exception.ErrorCode;
import com.example.smartair.repository.deviceRepository.DeviceRepository;
import com.example.smartair.repository.sensorRepository.SensorRepository;
import com.example.smartair.repository.roomRepository.RoomRepository;
import com.example.smartair.repository.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDeviceService {

    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final RoomRepository roomRepository;  
    private final UserRepository userRepository;  


    /**
     * 관리자용: 전체 기기 현황 상세 조회 (페이징)
     * @param pageable 페이징 및 정렬 정보
     * @return Page<DeviceDetailDto>
     */
    public Page<DeviceDetailDto> getAllDevicesDetailForAdmin(Pageable pageable) {
        Page<Device> devicePage = deviceRepository.findAll(pageable);

        return devicePage.map(device -> {
            DeviceDetailDto dto = DeviceDetailDto.from(device);
            if (device.getRoom().getId() != null && roomRepository != null) {
                roomRepository.findById(device.getRoom().getId())
                    .ifPresent(room -> dto.setRoomName(room.getName())); 
            }
            if (device.getRoom().getId() != null && userRepository != null) {
                userRepository.findById(device.getRoom().getId())
                    .ifPresent(user -> dto.setRegisteredUsername(user.getUsername()));
            }
            return dto;
        });
    }

    /**
     * 관리자용: 전체 센서 현황 상세 조회 (페이징)
     * @param pageable 페이징 및 정렬 정보
     * @return Page<SensorDetailDto>
     */
    public Page<SensorDetailDto> getAllSensorsDetailForAdmin(Pageable pageable) {
        Page<Sensor> sensorPage = sensorRepository.findAll(pageable);
        // SensorDetailDto.from() 이 Device 및 Room 정보를 잘 가져온다고 가정
        return sensorPage.map(SensorDetailDto::from);
    }

    public void setSensorActiveStatus(String serialNumber, boolean active) {
        Sensor sensor = sensorRepository.findBySerialNumber(serialNumber)
                .orElseThrow(()-> new CustomException(ErrorCode.SENSOR_NOT_FOUND));
        sensor.setRunningStatus(active);

        // 상태 변경 후 저장
        sensorRepository.save(sensor);
    }
} 
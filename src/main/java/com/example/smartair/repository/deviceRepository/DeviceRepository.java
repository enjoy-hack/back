package com.example.smartair.repository.deviceRepository;

import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceSerialNumber(String deviceSerialNumber);

    List<Device> findByRoomId(Long roomId);
}

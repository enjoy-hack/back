package com.example.smartair.repository.deviceRepository;

import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}

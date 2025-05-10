package com.example.smartair.repository.deviceRepository;

import com.example.smartair.entity.device.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface deviceRepository extends JpaRepository<Device, Long> {
}
